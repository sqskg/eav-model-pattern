package pl.softech.learning.domain.eav.dsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pl.softech.learning.domain.eav.dsl.AttributeDefinitionContext.Builder;
import pl.softech.learning.domain.eav.dsl.Token.Type;

/**
 * @author ssledz
 */
public class Parser {

	private Lexer lexer;
	private Token currentToken;
	private final ContextVisitor contextVisitor;

	public Parser(ContextVisitor contextVisitor) {
		this.contextVisitor = contextVisitor;
	}

	private boolean match(Type type) {
		return type == currentToken.getType();
	}

	private void consume(Type type) {
		if (!match(type)) {
			throw new RuntimeException(String.format("Should be %s is %s", type.name(), currentToken.getType().name()));
		}
		currentToken = lexer.next();
	}

	public void parse(InputStreamReader in) throws IOException {

		StringBuffer buffer = new StringBuffer();
		try (BufferedReader bin = new BufferedReader(in)) {
			String line;

			while ((line = bin.readLine()) != null) {
				buffer.append(line).append("\n");
			}
		}

		parse(buffer.toString());
	}

	public void parse(String in) {
		lexer = new Lexer(in);
		currentToken = lexer.next();
		conf();
	}

	private void conf() {

		while (!match(Type.EOF)) {

			if (match(Type.CATEGORY)) {
				catDef();
			} else if (match(Type.ATTRIBUTE)) {
				attDef();
			} else {
				objDef();
			}

		}

		consume(Type.EOF);

	}

	private void catDef() {
		consume(Type.CATEGORY);

		CategoryDefinitionContext.Builder builder = new CategoryDefinitionContext.Builder();
		builder.withIdentifier(currentToken.getValue());
		consume(Type.IDENTIFIER);

		nameProperty(builder);

		consume(Type.END);

		new CategoryDefinitionContext(builder).accept(contextVisitor);
	}

	private void attDef() {
		consume(Type.ATTRIBUTE);

		AttributeDefinitionContext.Builder builder = new AttributeDefinitionContext.Builder();

		builder.withIdentifier(currentToken.getValue());
		consume(Type.IDENTIFIER);

		while (!match(Type.END)) {

			if (match(Type.NAME)) {
				nameProperty(builder);
			} else if (match(Type.CATEGORY)) {
				categoryProperty(builder);
			} else {
				dataTypeProperty(builder);
			}

		}

		consume(Type.END);

		new AttributeDefinitionContext(builder).accept(contextVisitor);

	}

	private void dataTypeProperty(Builder builder) {
		consume(Type.DATA_TYPE);
		consume(Type.COLON);

		if (match(Type.DICTIONARY)) {

			consume(Type.DICTIONARY);
			consume(Type.OF);
			builder.withDataTypePropertyContext(new DictionaryDataTypePropertyContext(currentToken.getValue()));
			consume(Type.STRING);

		} else {

			builder.withDataTypePropertyContext(new DataTypePropertyContext(currentToken.getValue()));
			consume(Type.IDENTIFIER);

		}

	}

	private void categoryProperty(Builder builder) {
		consume(Type.CATEGORY);
		consume(Type.COLON);
		builder.withCategoryPropertyContext(new CategoryPropertyContext(currentToken.getValue()));
		consume(Type.STRING);
	}

	private void nameProperty(NamePropertyContextAware<?> builder) {
		consume(Type.NAME);
		consume(Type.COLON);
		builder.withNamePropertyContext(new NamePropertyContext(currentToken.getValue()));
		consume(Type.STRING);
	}

	private void objDef() {
		consume(Type.OBJECT);

		ObjectDefinitionContext.Builder builder = new ObjectDefinitionContext.Builder();
		builder.withObjectIdentifier(currentToken.getValue());
		consume(Type.IDENTIFIER);
		consume(Type.OF);
		builder.withCategoryIdentifier(currentToken.getValue());
		consume(Type.IDENTIFIER);
		consume(Type.CATEGORY);

		while (!match(Type.END)) {

			ObjectBodyContext.Builder innerBuilder = new ObjectBodyContext.Builder();
			innerBuilder.withAttributeIdentifier(currentToken.getValue());

			if (match(Type.IDENTIFIER)) {
				consume(Type.IDENTIFIER);
			} else {
				consume(Type.NAME);
			}
			consume(Type.COLON);
			innerBuilder.withValue(currentToken.getValue());
			consume(Type.STRING);

			builder.add(new ObjectBodyContext(innerBuilder));

		}

		consume(Type.END);

		new ObjectDefinitionContext(builder).accept(contextVisitor);
	}

}