package org.reflections.helper;

import com.google.common.base.Nullable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.reflections.model.Configuration;

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

    public static <T> Iterable<T> filterAll(Iterable<T> list, final Filter<T> filter) {
        return Iterables.filter(list,new Predicate<T>() {
            public boolean apply(@Nullable T t) {
                return filter.accepts(t);
            }
        });
    }
}
