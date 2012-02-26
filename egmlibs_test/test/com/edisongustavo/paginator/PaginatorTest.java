package com.edisongustavo.paginator;
import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.edisongustavo.paginator.PaginatedProvider;
import com.edisongustavo.paginator.Paginator;

public class PaginatorTest {
	private SimpleProvider provider;
	private Paginator<StringAndWeight, Float> paginator;

	private class StringAndWeight {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private PaginatorTest getOuterType() {
			return PaginatorTest.this;
		}
	}

	private class SimpleProvider implements
			PaginatedProvider<StringAndWeight, Float> {

		private List<StringAndWeight> results = new ArrayList<StringAndWeight>();

		@Override
		public List<StringAndWeight> provide(Float parameter, Integer limit) {
			ArrayList<StringAndWeight> ret = new ArrayList<StringAndWeight>();
			for (StringAndWeight element : results) {
				if (parameter == null
						|| element.weight.compareTo(parameter) <= 0)
					ret.add(element);

				if (limit != null && ret.size() == limit)
					break;
			}
			return ret;
		}

		public void add(float weight, String value) {
			StringAndWeight obj = new StringAndWeight();
			obj.weight = weight;
			obj.string = value;
			results.add(obj);

			with(results).sort(on(StringAndWeight.class).getWeight());
			Collections.reverse(results);
		}

		@Override
		public Float getParameter(StringAndWeight obj) {
			return obj.weight;
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

		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
		assertThat(all, containsInAnyOrder("A", "B", "C", "D", "E"));
	}

	@Test
	public void iteratorWithRepeatedWeights() {
		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(3, "D");
		provider.add(4, "E");

		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
		assertThat(all, containsInAnyOrder("A", "B", "C", "D", "E"));
	}

	@Test
	public void iteratorWithRepeatedWeightsLargerThanPaginationSize() {
		provider.add(1, "A");
		provider.add(2, "B");
		provider.add(2, "C");
		provider.add(2, "D");
		provider.add(2, "E");
		provider.add(3, "F");

		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
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

		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
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

		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
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
		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
		assertThat(all, containsInAnyOrder("A", "B"));
	}

	@Test
	public void emptyCollection() {
		List<String> all = extract(paginator.iterator(),
				on(StringAndWeight.class).getString());
		assertTrue(all.isEmpty());
	}
}
