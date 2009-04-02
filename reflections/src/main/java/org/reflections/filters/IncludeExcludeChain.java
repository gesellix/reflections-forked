package org.reflections.filters;

import org.reflections.ReflectionsException;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class IncludeExcludeChain<T> implements Filter<T> {
    private final List<IncludeExcludeFilter<T>> includeExcludeFilters;
    private final boolean startWith;

    public IncludeExcludeChain(final IncludeExcludeFilter<T>... includeExcludeFilters) {
        if (includeExcludeFilters == null || includeExcludeFilters.length == 0) {
            throw new ReflectionsException("IncludeExcludeChain must contain at least 1 filter.");
        }

        final IncludeExcludeFilter<T> first = includeExcludeFilters[0];
        startWith = first instanceof ExcludeFilter; //start with the opposite of the first filter

		this.includeExcludeFilters = ImmutableList.of(includeExcludeFilters);
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
