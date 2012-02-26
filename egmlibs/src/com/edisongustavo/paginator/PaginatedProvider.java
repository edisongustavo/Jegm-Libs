package com.edisongustavo.paginator;
import java.util.List;

public interface PaginatedProvider<T, P> {
	List<T> provide(P parameter, Integer limit);
	
	P getParameter(T obj);
}
