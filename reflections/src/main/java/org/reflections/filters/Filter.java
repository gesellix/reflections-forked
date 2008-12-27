package org.reflections.filters;

/**
 *
 */
public interface Filter<T> {
    boolean accept(T t);
}
