package org.reflections.filters;

/**
 *
 */
public class IncludePrefix implements IncludeFilter<String> {
    private final String prefix;

    public IncludePrefix(final String prefix) {
        this.prefix = prefix;
    }

    public boolean accept(final String name) {
        return name.startsWith(prefix);
    }
}
