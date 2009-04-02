package org.reflections.util;

import com.google.common.collect.AbstractIterator;
import org.reflections.filters.Filter;

import java.util.Iterator;

/**
 * an abstract wrapper class for Iterable fluent manipulation.
 * helps to create a chain of iterables in a fluent manner.
 */
public abstract class FluentIterable<T> implements Iterable<T> {

    /**
     * a starting point of FluentIterator. wraps an iterable into a FluentIterable.
     */
    public static <T> FluentIterable<T> iterate(final Iterable<T> iterable) {
        return new FluentIterable<T>() {
            public Iterator<T> iterator() {
                return iterable.iterator();
            }
        };
    }

    /**
     * a starting point of FluentIterator. wraps an iterator into a FluentIterable.
     */
    public static <T> FluentIterable<T> iterate(final Iterator<T> iterator) {
        return new FluentIterable<T>() {
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }

    /**
     * forks each element T of this into an iterable of T2 using a given transformer
     */
    public <T2> FluentIterable<T2> fork(final Transformer<T, Iterator<T2>> transformer) {
        return iterate(
                new ForkIterator<T, T2>(FluentIterable.this.iterator()) {
                    public Iterator<T2> transform(final T t) {
                        return transformer.transform(t);
                    }
                });
    }

    /**
     * filters each element T of this using a given filter
     */
    public FluentIterable<T> filter(final Filter<T> filter) {
        return iterate(
                new FilterIterator<T>(FluentIterable.this.iterator(), filter));
    }

    /**
     * transforms each element of T to T2 using a given transformer
     */
    public <T2> FluentIterable<T2> transform(final Transformer<T, T2> transformer) {
        return iterate(
                new TransformIterator<T, T2>(FluentIterable.this.iterator(), transformer));
    }

    //
	public abstract static class ForkIterator<T1, T2> extends AbstractIterator<T2> implements Transformer<T1, Iterator<T2>> {
        private final Iterator<T1> iterator1;
        private Iterator<T2> iterator2;

        public ForkIterator(final Iterator<T1> iterator1) {
            this.iterator1 = iterator1;
            this.iterator2 = new EmptyIterator<T2>();
        }

        protected T2 computeNext() {
            while (!iterator2.hasNext()) {
                if (!iterator1.hasNext()) {
                    return endOfData();
                }

                T1 t1 = iterator1.next();
                iterator2 = transform(t1);
            }

            return iterator2.next();
        }
    }

    public static class EmptyIterator<T2> extends AbstractIterator<T2> {
        protected T2 computeNext() {return endOfData();}
    }

    public static class FilterIterator<T> extends AbstractIterator<T> {
        private final Iterator<T> iterator;
        private final Filter<T> filter;

        public FilterIterator(final Iterator<T> iterator, final Filter<T> filter) {
            this.iterator = iterator;
            this.filter = filter;
        }

        protected T computeNext() {
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (filter.accept(t)) {
                    return t;
                }
            }

            return endOfData();
        }
    }

    public static class TransformIterator<T, T2> extends AbstractIterator<T2> {

        private final Iterator<T> iterator;
        private final Transformer<T, T2> transformer;

        public TransformIterator(final Iterator<T> iterator, final Transformer<T, T2> transformer) {
            this.iterator = iterator;
            this.transformer = transformer;
        }

        protected T2 computeNext() {
            return iterator.hasNext() ? transformer.transform(iterator.next()) : endOfData();
        }
    }
}
