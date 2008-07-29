package org.reflections.helper;

import java.util.HashMap;

/**
 * @author mamo
*/
public abstract class StupidLazyMap<K, V> extends HashMap<K, V> {
    protected abstract V create(K key);

    @SuppressWarnings({"unchecked"})
    @Override
    public V get(final Object key) {
        if (containsKey(key)) {
            return super.get(key);
        } else {
            V value = create((K) key);
            put((K) key, value);
            return value;
        }
    }
}
