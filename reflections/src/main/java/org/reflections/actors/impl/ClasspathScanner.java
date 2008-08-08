package org.reflections.actors.impl;

import org.reflections.actors.Scanner;
import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;
import org.reflections.helper.Logs;

import java.util.Set;

import com.google.common.collect.Sets;

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
        long start = System.currentTimeMillis();
        long time = start;

        Set<String> postCompiledResources = Sets.newHashSet();
        if (configuration.shouldFetchPreCompiledResources()) {
            postCompiledResources = new PostCompiledResourcesCollector(configuration, classpathMD).collect();

            Logs.info(String.format("Post compiled resources collector took %d ms to collect %d resources",
                    -time + (time = System.currentTimeMillis()) , postCompiledResources.size()));
        }

        if (!configuration.getElementTypesToScan().isEmpty()) {
            new JavassistClassScanner(configuration,classpathMD).scan();

            Logs.info(String.format("Class scanner took %d ms to scan %d classes",
                    -time + (time = System.currentTimeMillis()) , classpathMD.getClassCount()));
        }

        if (configuration.shouldComputeTransitiveClosure()) {
            new TransativeClosureScanner(classpathMD).scan();

            Logs.info(String.format("Transitive closure scanner took %d ms",
                    -time + (time = System.currentTimeMillis())));
        }

        if (configuration.shouldComputeInvertedIndices()) {
            new InvertedIndicesScanner(configuration,classpathMD).scan();

            Logs.info(String.format("Compute inverted indices took %d ms",
                    -time + (time = System.currentTimeMillis())));
        }

        Logs.info(String.format("Reflections took %d ms to find %d classes in %d urls and %d post compiled resources", 
                System.currentTimeMillis() - start , classpathMD.getClassCount(),configuration.getUrls().size(),postCompiledResources.size()));
    }
}
