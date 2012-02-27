package com.edisongustavo.paginator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractProvider<T, P extends Comparable<? super P>>
		implements PaginatedProvider<T, P> {

	protected List<T> results = new ArrayList<T>();

	@Override
	public List<T> provide(P parameter, int limit) {
		ArrayList<T> ret = new ArrayList<T>();
		for (T element : results) {
			if (parameter == null
					|| getParameter(element).compareTo(parameter) <= 0)
				ret.add(element);

			if (ret.size() == limit)
				break;
		}
		return ret;
	}

	public void add(T obj) {
		results.add(obj);
		Collections.sort(results, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				P p1 = getParameter(o1);
				P p2 = getParameter(o2);
				return p1.compareTo(p2);
			}
		});
		Collections.reverse(results);
	}
}
