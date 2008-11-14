package org.reflections.filters;

import java.util.Collection;

/**
 *
 */
public class IncludeExcludeChain<T> implements Filter<T> {
    private final IncludeExcludeFilter<T>[] includeExcludeFilters;
    private final boolean startWith;

    public IncludeExcludeChain(final IncludeExcludeFilter<T>... includeExcludeFilters) {
        if (includeExcludeFilters == null || includeExcludeFilters.length == 0) {
            throw new RuntimeException();
        }

        final IncludeExcludeFilter<T> first = includeExcludeFilters[0];
        startWith = first instanceof ExcludeFilter; //start with the opposite of the first filter

        this.includeExcludeFilters = includeExcludeFilters;
    }

    public IncludeExcludeChain(final Collection<IncludeExcludeFilter<T>> filters) {
        //noinspection unchecked
        this(filters.toArray(new IncludeExcludeFilter[filters.size()]));
    }

    public boolean accept(final T name) {
        boolean accept = startWith;
        for (IncludeExcludeFilter<T> filter : includeExcludeFilters) {
            //skip if this filter won't change
            if (accept && filter instanceof IncludeFilter) {
                continue;
            }
            if (!accept && filter instanceof ExcludeFilter) {
                continue;
            }

            accept = filter.accept(name);
        }

        return accept;
    }
}
