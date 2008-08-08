package org.reflections.actors.impl;

import org.reflections.actors.Collector;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Collects post compiled resources from jars generated via the ReflectionsMojo maven plugin, for example.
 * Uses package prefix and resources pattern to fetch matching jar resources
 *
 * @author mamo
 */
public class PostCompiledResourcesCollector implements Collector {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public PostCompiledResourcesCollector(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }

    public Set<String> collect() {
        String pattern = configuration.getPostCompiledResourcesPattern();

        //todo: normalization should occur somewhere else, if any
        if (pattern.startsWith("/")) {
            pattern = pattern.substring(1);
        }

        final Set<String> resources = ClasspathHelper.getMatchingJarResources(
                configuration.getUrls(),configuration.getPostCompiledResourcesPackagePrefix(),Pattern.compile(pattern));

        final XmlMarshaller marshaller = new XmlMarshaller(classpathMD);
        for (String resource : resources) {
            try {
                marshaller.load(resource);
            }
            catch (Exception ex) {
                throw new RuntimeException("error while unmarshalling resource",ex); //todo: better log
            }
        }

        return resources;
    }
}