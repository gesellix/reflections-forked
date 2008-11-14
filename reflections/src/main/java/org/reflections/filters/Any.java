package org.reflections.filters;

/**
 *
 */
public class Any<T> implements Filter<T> {
    public boolean accept(final T name) {
        return true;
    }
}
