package pl.softech.learning.domain.eav;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;

import pl.softech.learning.domain.AbstractEntity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@Entity
public class MyObject extends AbstractEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "object")
	private Set<ObjectValue> values = Sets.newHashSet();

	private String name;

	protected MyObject() {
	}

	public MyObject(Category category, String name) {
		this.category = checkNotNull(category);
		this.name = checkNotNull(name);
	}

	public Category getCategory() {
		return category;
	}

	public ImmutableSet<ObjectValue> getValues() {
		return new ImmutableSet.Builder<ObjectValue>().addAll(values).build();
	}

	public <T extends AbstractValue<?>> ObjectValue addValue(final Attribute attribute, T value) {

		checkNotNull(attribute);
		checkNotNull(value);

		ValueMatchAttributeSpecification spec = new ValueMatchAttributeSpecification();

		checkArgument(spec.isSafisfiedBy(Pair.of(value, attribute)),
				String.format("Attribute %s doesn't match the value %s", attribute.toString(), value.toString()));

		final ObjectValue[] bag = new ObjectValue[1];

		ValueVisitor visitor = new ValueVisitor() {

			@Override
			public void visit(DateValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}

			@Override
			public void visit(DictionaryEntryValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}

			@Override
			public void visit(DoubleValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}

			@Override
			public void visit(IntegerValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}

			@Override
			public void visit(BooleanValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}

			@Override
			public void visit(StringValue value) {
				bag[0] = new ObjectValue(attribute, MyObject.this, value);
			}
		};

		value.accept(visitor);

		checkNotNull(bag[0]);

		values.add(bag[0]);

		return bag[0];
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		sb.appendSuper(super.toString());
		sb.append("name", name);
		sb.append("category", category);
		return sb.toString();
	}
}
