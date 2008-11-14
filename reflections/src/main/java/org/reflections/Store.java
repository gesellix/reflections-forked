package org.reflections;

import java.util.*;

/**
 *
 */
public class Store {
    private final Map<String/*indexName*/, Map<String/*key*/,Set<String>/*values*/>> store = 
            new HashMap<String, Map<String, Set<String>>>();

    public Map<String, Set<String>> get(String indexName) {
        if (!store.containsKey(indexName)) {
            store.put(indexName, new HashMap<String, Set<String>>());
        }

        return store.get(indexName);
    }

    public void merge(final Store outer) {
        for (String indexName : outer.store.keySet()) {
            get(indexName).putAll(outer.get(indexName));
        }
    }
}
