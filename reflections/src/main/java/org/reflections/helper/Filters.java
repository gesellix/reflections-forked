package org.reflections.helper;

import com.google.common.collect.Sets;
import org.reflections.model.Configuration;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mamo
 */
public abstract class Filters {

    public interface Filter<T> {
        boolean accepts(T t);
    }

    public static final Filter<String> classFileFilter = new Filter<String>() {
        public boolean accepts(String name) {return name.endsWith(".class");}
    };

    /**
     * include all, exclude, include
     */
    public static class IncludeAllExcludeIncludePatternFilter implements Filter<String> {
        private final Configuration configuration;

        public IncludeAllExcludeIncludePatternFilter(Configuration configuration) {this.configuration = configuration;}

        private StupidLazyMap<String,Boolean> filteredCache = new StupidLazyMap<String, Boolean>() {
            protected Boolean create(String fileName) {
                boolean accepts = true;

                for (Pattern fqnPrefix : configuration.getExcludePatterns()) {
                    if (fqnPrefix.matcher(fileName).matches()) {
                        accepts = false; break;
                    }
                }

                for (Pattern fqnPrefix : configuration.getIncludePatterns()) {
                    if (fqnPrefix.matcher(fileName).matches()) {
                        accepts = true; break;
                    }
                }

                return accepts;
            }
        };

        public boolean accepts(String fileName) {
            return filteredCache.get(fileName);
        }
    }

    public static <T> Set<T> filter(Iterable<T> list, final Filter<T> filter) {
        Set<T> result = Sets.newHashSet();
        for (T item : list) {
            if (filter.accepts(item)) {result.add(item);}
        }

        return result;
    }

    public static class Aggregation implements Filters.Filter<String> {
        private final Filters.Filter<String>[] filters;

        public Aggregation(Filters.Filter<String>... filters) {this.filters = filters;}

        public boolean accepts(String s) {
            for (Filters.Filter<String> filter : filters) {
                if (!filter.accepts(s)) {return false;}
            }
            return true;
        }
    }

}
