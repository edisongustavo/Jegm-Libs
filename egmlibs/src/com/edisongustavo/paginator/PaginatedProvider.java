package com.edisongustavo.paginator;

import java.util.List;

/**
 * Provides items in bulks sorted by the parameter returned by
 * {@link PaginatedProvider#getParameter(Object)}
 * 
 * @author Edison Gustavo Muenz <edisongustavo@gmail.com>
 * 
 * @param <T>
 *            The paginated type
 * @param <P>
 *            The parameter that we will paginate
 */
public interface PaginatedProvider<T, P extends Comparable<? super P>> {
	/**
	 * The list of items returned must be ordered by <code>parameter</code>
	 * 
	 * @param parameter
	 *            An optional parameter so that the results are only returned
	 * @param limit
	 *            The maximum number of items to be returned. Note that
	 *            respecting this number is not strictly necessary, but not
	 *            using it it defeats the whole purpose of the paginator
	 * @return
	 */
	List<T> provide(P parameter, int limit);

	P getParameter(T obj);
}
