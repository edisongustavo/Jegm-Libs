package com.edisongustavo.paginator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Paginator<T, P extends Comparable<? super P>> implements
		Iterator<T> {

	private enum ParameterOrder {
		ASCENDING, DESCENDING
	}

	private final PaginatedProvider<T, P> provider;
	private final int limit;
	private int actualLimit;
	private P parameter;
	private ParameterOrder parameterOrder;
	private Integer maxLimit;

	public Paginator(PaginatedProvider<T, P> provider, int limit) {
		this(provider, limit, null);
	}

	public Paginator(PaginatedProvider<T, P> provider, int limit,
			Integer maxLimit) {
		this.provider = provider;
		this.maxLimit = maxLimit;
		this.limit = this.actualLimit = limit;
	}

	@Override
	public boolean hasNext() {
		if (!items.isEmpty())
			return true;

		fetch();

		return !items.isEmpty();
	}

	@Override
	public T next() {
		if (!items.isEmpty())
			return items.pop();

		fetch();

		return items.pop();
	}

	private void fetch() {
		while (items.size() < limit) {
			boolean b = tryFetch(items, uniqueFetchedItems);
			if (b)
				break;
		}
	}

	@Override
	public void remove() {
		items.remove();
	}

	private List<T> uniqueFetchedItems = new ArrayList<T>();
	private LinkedList<T> items = new LinkedList<T>();

	private boolean tryFetch(List<T> ret, List<T> uniqueFetchedItems) {
		List<T> fetchedItems = provider.provide(parameter, actualLimit);

		int numberOfFetchedItems = fetchedItems.size();

		fetchedItems.removeAll(uniqueFetchedItems);
		uniqueFetchedItems.addAll(fetchedItems);

		ret.addAll(fetchedItems);

		if (fetchedItems.isEmpty()) {

			// We reached the end of the items list
			if (numberOfFetchedItems < actualLimit)
				return true;

			// We couldn't fetch anything, so we'll try to fetch more items
			// the next time
			if (maxLimit != null) {
				if (actualLimit == maxLimit)
					return true;
				actualLimit = Math.min(maxLimit, actualLimit * 2);
			} else {
				actualLimit *= 2;
			}

		} else {
			resetLimit(); // TODO [edison]: should I reset the limit or just
							// let it fetch as many items as it wants on
							// subsequent steps?

			parameter = getNextParameter(fetchedItems);
		}

		return false;
	}

	private void resetLimit() {
		if (actualLimit != limit)
			actualLimit = limit;
	}

	private P getNextParameter(List<T> l) {
		if (l == null || l.isEmpty())
			return null;

		if (l.size() == 1)
			return provider.getParameter(l.get(0));

		if (parameter == null)
			return guessParameter(l);

		if (parameterOrder == ParameterOrder.ASCENDING)
			return provider.getParameter(l.get(0));
		else
			return provider.getParameter(l.get(l.size() - 1));
	}

	private P guessParameter(List<T> l) {
		P firstParam = provider.getParameter(l.get(0));
		P lastParam = provider.getParameter(l.get(l.size() - 1));

		if (firstParam.compareTo(lastParam) < 0) {
			parameterOrder = ParameterOrder.ASCENDING;
			return firstParam;
		} else {
			parameterOrder = ParameterOrder.DESCENDING;
			return lastParam;
		}
	}

	public void setMaxLimit(int limit) {
		this.maxLimit = limit;
	}
}
