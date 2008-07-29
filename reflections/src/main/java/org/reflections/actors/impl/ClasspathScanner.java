package org.reflections.actors.impl;

import org.reflections.actors.Scanner;
import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;

/**
 * @author mamo
 */
public class ClasspathScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public ClasspathScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }

    public void scan() {
        if (configuration.shouldFetchPreCompiledResources()) {
            new PostCompiledResourcesCollector(configuration,classpathMD).collect();
        }

        //todo: avoid scanning urls with post compiled resources, maybe
        //  configuration._postCompiledResources=resources;

        if (configuration.shouldScanSources() && !configuration.getElementTypesToScan().isEmpty()) {
            new JavaAssistClassScanner(configuration,classpathMD).scan();
        }

        if (configuration.shouldComputeTransitiveClosure()) {
            new TransativeClosureScanner(classpathMD).scan();
        }

        if (configuration.shouldComputeInvertedIndexes()) {
            new InvertedIndexesScanner(configuration,classpathMD).scan();
        }
    }
}
