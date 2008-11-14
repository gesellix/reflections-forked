package org.reflections.scanners;

import org.reflections.Configuration;

import java.util.Set;
import java.util.Map;

/**
 *
 */
public interface Scanner {

    void scan(final Object cls);

    void setConfiguration(Configuration configuration);

    void setStore(Map<String, Set<String>> store);

    String getIndexName();
}
