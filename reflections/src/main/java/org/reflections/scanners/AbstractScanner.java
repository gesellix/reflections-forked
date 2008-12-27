package org.reflections.scanners;

import org.reflections.Configuration;
import org.reflections.adapters.MetadataAdapter;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;

/**
 *
 */
@SuppressWarnings({"RawUseOfParameterizedType", "unchecked"})
public abstract class AbstractScanner implements Scanner {
    private Configuration configuration;
    protected Map<String, Set<String>> store;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void setStore(final Map<String, Set<String>> store) {
        this.store = store;
    }

    protected MetadataAdapter getMetadataAdapter() {
        return configuration.getMetadataAdapter();
    }

    protected void populate(final String key, final String value) {
        multiRelaxAdd(store, key, value);
    }

    protected boolean accept(final String fqn) {
        return fqn!=null && configuration.getFilter().accept(fqn);
    }

    protected <T> void multiRelaxAdd(final Map<T, Set<T>> map, final T key, final T... values) {
        final Set<T> set = new HashSet<T>(values.length);
        for (T value : values) {
            if (value != null) {
                set.add(value);
            }
        }

        if (!set.isEmpty()) {
            if (!map.containsKey(key)) {
                map.put(key, set);
            } else {
                map.get(key).addAll(set);
            }
        }
    }
}
