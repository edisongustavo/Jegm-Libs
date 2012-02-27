package com.edisongustavo.paginator;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PaginatorTest {
	private SimpleProvider provider;
	private Paginator<StringAndWeight, Float> paginator;

	private static class StringAndWeight {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((string == null) ? 0 : string.hashCode());
			result = prime * result
					+ ((weight == null) ? 0 : weight.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StringAndWeight other = (StringAndWeight) obj;
			if (string == null) {
				if (other.string != null)
					return false;
			} else if (!string.equals(other.string))
				return false;
			if (weight == null) {
				if (other.weight != null)
					return false;
			} else if (!weight.equals(other.weight))
				return false;
			return true;
		}

		public Float weight;
		public String string;

		public Float getWeight() {
			return weight;
		}

		public String getString() {
			return string;
		}

		@Override
		public String toString() {
			return "" + weight + "->" + string;
		}
	}

	private class SimpleProvider extends
			AbstractProvider<StringAndWeight, Float> {

		@Override
		public Float getParameter(StringAndWeight obj) {
			return obj.weight;
		}

		public void add(float weight, String value) {
			StringAndWeight obj = new StringAndWeight();
			obj.weight = weight;
			obj.string = value;

			super.add(obj);
		}
	}

	@Before
	public void setUp() {
		provider = new SimpleProvider();
		paginator = new Paginator<StringAndWeight, Float>(provider, 3);
	}

	@Test
	public void iterator() {
		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(3, "C");
		provider.add(4, "D");
		provider.add(5, "E");

		assertTrue(paginator.hasNext());

		assertEquals("E", paginator.next().string);
		assertEquals("D", paginator.next().string);
		assertEquals("C", paginator.next().string);
		assertEquals("B", paginator.next().string);
		assertEquals("A", paginator.next().string);

		assertFalse(paginator.hasNext());
	}

	@Test
	public void iteratorWithRepeatedWeights() {
		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(3, "D");
		provider.add(4, "E");

		assertEquals("E", paginator.next().string);
		assertEquals("D", paginator.next().string);

		List<String> items = extract(collectFirst(paginator, 2),
				on(StringAndWeight.class).getString());
		assertThat(items, containsInAnyOrder("C", "B"));

		assertEquals("A", paginator.next().string);
	}

	private static <T> List<T> collectFirst(Iterator<T> it, int number) {
		List<T> ret = new ArrayList<T>();
		while (number-- > 0)
			ret.add(it.next());
		return ret;
	}

	@Test
	public void iteratorWithRepeatedWeightsLargerThanPaginationSize() {
		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(2, "D");
		provider.add(2, "E");
		provider.add(3, "F");

		List<String> all = extract(paginator, on(StringAndWeight.class)
				.getString());
		assertThat(all, containsInAnyOrder("A", "B", "C", "D", "E", "F"));
	}

	@Test
	public void maxLimitCanMakeThePaginatorSurpassRepeatingItems() {
		paginator.setMaxLimit(4);

		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(2, "D");
		provider.add(3, "E");

		List<String> all = extract(paginator, on(StringAndWeight.class)
				.getString());
		assertThat(all, containsInAnyOrder("A", "B", "C", "D", "E"));
	}

	@Test
	public void maxLimitSmallerThanNumberOfRepeatingItems() {
		paginator.setMaxLimit(4);

		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(2, "D");
		provider.add(2, "E");
		provider.add(2, "F");
		provider.add(3, "G");

		List<String> all = extract(paginator, on(StringAndWeight.class)
				.getString());
		assertThat(all, hasItem("G"));
		assertThat(all, anyOf( //
				hasItem("B") //
				, hasItem("C") //
				, hasItem("D") //
				, hasItem("E") //
				, hasItem("F") //
				));
		assertThat(all, not(hasItem("A")));
		assertTrue(4 <= all.size() && all.size() <= 6);
	}

	@Test
	public void limitLargerThanNumberOfItems() {
		provider.add(1, "A");
		provider.add(2, "B");
		List<String> all = extract(paginator, on(StringAndWeight.class)
				.getString());
		assertThat(all, containsInAnyOrder("A", "B"));
	}

	@Test
	public void hasNextOnEmptyCollection() {
		assertFalse(paginator.hasNext());
	}
}
