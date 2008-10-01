package org.reflections.model;

import com.google.common.collect.Iterables;
import org.reflections.helper.ClasspathHelper;
import static org.reflections.model.ElementTypes.annotations;

import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

/**
 * @author mamo
 */
public enum ConfigurationBuilder {
    Default
            {
                @Override
                public Configuration build(Configuration configuration) {
                    Annotations.build(configuration);
                    ExcludeJRE.build(configuration);

                    return configuration;
                }
            },
    Development
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addUrls(ClasspathHelper.getUrlsForSourcesOnly());
//                            .setScanSources(true);
                }
            },
    ////
    PostCompiled
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .setFetchPostCompiledResources(true);
                }
            },
    ThisUrl
            {
                @Override
                public Configuration build(Configuration configuration) {
                    String callee = null;
                    for (StackTraceElement stackTraceElement : Iterables.cycle(Thread.currentThread().getStackTrace())) {
                        final String className = stackTraceElement.getClassName();
                        if (!className.startsWith("org.reflections") && !className.startsWith("java.lang")
                                || className.startsWith("org.reflections.tests")) // sorry about that, this is for test purposes
                        {
                            callee = className;
                            break;
                        }
                    }

                    return configuration
                            .addUrls(ClasspathHelper.getUrlForClassName(callee));
//                            .setScanSources(true);
                }
            },
    //
    Transitive
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration.setComputeTransitiveClosure(true);
                }
            },
//    Sources
//            {
//                @Override
//                public Configuration build(Configuration configuration) {
//                    return configuration.setScanSources(true);
//                }
//            },
    Annotations
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addElementTypesToScan(annotations)
                            .addReverseElementTypes(annotations);
                }
            },
    ExcludeAll
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addExcludePatterns(Pattern.compile(".*"));
                }
            },
    ExcludeJRE
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addExcludePatterns(compile("java\\..*"), compile("javax\\..*"), compile("sun\\..*"), compile("com.sun\\..*"));
                }
            };

    ////
    public abstract Configuration build(Configuration configuration);

    public static Configuration build(ConfigurationBuilder... builders) {
        final Configuration configuration = new Configuration() {};

        for (ConfigurationBuilder builder : builders) {builder.build(configuration);}

        return configuration;
    }
}

