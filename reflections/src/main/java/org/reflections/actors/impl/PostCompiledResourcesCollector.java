package org.reflections.actors.impl;

import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mamo
 */
public class PostCompiledResourcesCollector {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public PostCompiledResourcesCollector(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }


    public void collect() {
        String pattern = configuration.getPostCompiledResourcesPattern();

        //todo: normalization should occur somewhere else, if any
        if (pattern.startsWith("/")) {
            pattern = pattern.substring(1);
        }

        final Set<String> resources = ClasspathHelper.getMatchingJarResources(
                configuration.getUrls(),configuration.getPostCompiledResourcesPackagePrefix(),Pattern.compile(pattern));

        //todo: cahce it somewhere, maybe
//        configuration._postCompiledResources=resources;

        for (String resource : resources) {
            try {
                new XmlMarshaller(classpathMD).load(resource);
            }
            catch (Exception ex) {
                throw new RuntimeException("error while unmarshalling resource",ex); //todo: better log
            }
        }
    }
}