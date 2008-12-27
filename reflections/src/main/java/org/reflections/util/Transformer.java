package org.reflections.util;

/**
 *
 */
public interface Transformer<F,T> {
    T transform(F f);
}
