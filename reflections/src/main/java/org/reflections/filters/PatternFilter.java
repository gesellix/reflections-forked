package org.reflections.filters;

import java.util.regex.Pattern;

/**
 *
 */
public class PatternFilter implements Filter<String> {
    private final Pattern pattern;

    public PatternFilter(final String patternString) {
        pattern = Pattern.compile(patternString);
    }

    public boolean accept(final String name) {
        return pattern.matcher(name).matches();
    }
}
