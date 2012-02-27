package com.edisongustavo.paginator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class DateTimePaginatorTest {
	private static class StringAndDateTime {
		public DateTime date;
		public String string;

		public DateTime getDate() {
			return date;
		}

		public String getString() {
			return string;
		}

		@Override
		public String toString() {
			return "" + date + "->" + string;
		}
	}

	private static class DateTimeProvider extends
			AbstractProvider<StringAndDateTime, DateTime> {

		@Override
		public DateTime getParameter(StringAndDateTime obj) {
			DateTime date = obj.getDate();
			return date;
		}

		public void add(DateTime date, String str) {
			StringAndDateTime obj = new StringAndDateTime();
			obj.date = date;
			obj.string = str;
			super.add(obj);
		}
	}

	private DateTimeProvider provider;
	private Paginator<StringAndDateTime, DateTime> paginator;

	@Before
	public void setUp() {
		provider = new DateTimeProvider();
		paginator = new Paginator<StringAndDateTime, DateTime>(provider, 3);
	}

	@Test
	public void iterator() {
		DateTime now = new DateTime();
		provider.add(now.plusSeconds(1), "A");
		provider.add(now.plusSeconds(2), "B");
		provider.add(now.plusSeconds(3), "C");
		provider.add(now.plusSeconds(4), "D");
		
		assertTrue(paginator.hasNext());
		assertEquals("D", paginator.next().string);
		assertEquals("C", paginator.next().string);
		assertEquals("B", paginator.next().string);
		assertEquals("A", paginator.next().string);
	}
}
