package org.reflections;

import com.google.common.base.Function;
import com.google.common.base.Nullable;
import com.google.common.base.Supplier;
import com.google.common.collect.*;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 *
 */
public class Store {

	private final Map<String/*indexName*/, Multimap<String, String>> store = new MapMaker().makeComputingMap(new Function<String, Multimap<String, String>>() {
		public Multimap<String, String> apply(@Nullable String indexName) {
			return Multimaps.newSetMultimap(new MapMaker().<String, Collection<String>>makeMap(), new Supplier<Set<String>>() {
				public Set<String> get() {
					return Sets.newConcurrentHashSet();
				}
			});
		}
	});

	public Multimap<String, String> get(String indexName) {
		return store.get(indexName);
	}

	public void merge(final Store outer) {
		for (String indexName : outer.store.keySet()) {
			get(indexName).putAll(outer.get(indexName));
		}
	}
}
