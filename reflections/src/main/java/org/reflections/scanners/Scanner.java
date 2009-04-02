package org.reflections.scanners;

import com.google.common.collect.Multimap;
import org.reflections.Configuration;
import org.reflections.filters.Filter;

/**
 *
 */
public interface Scanner {

	void scan(final Object cls);

	void setConfiguration(Configuration configuration);

	void setStore(Multimap<String, String> store);

	String getIndexName();

	Scanner filterBy(Filter<String> filter);
}
