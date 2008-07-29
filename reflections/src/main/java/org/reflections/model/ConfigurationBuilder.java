package org.reflections.model;

import org.reflections.helper.ClasspathHelper;
import static org.reflections.model.ElementTypes.annotations;
import com.google.common.collect.Iterables;

/**
 * @author mamo
 */
public enum ConfigurationBuilder {
    Empty
            {
                @Override
                public Configuration build(Configuration configuration) {
                    //noinspection EmptyClassInitializer
                    return new Configuration() {
                        {}
                    };
                }},
    /**
     * Default is by default, it's not really deprecated
     */
    @Deprecated
    Default
            {
                @Override
                @SuppressWarnings({"AssignmentToMethodParameter"})
                public Configuration build(Configuration configuration) {
                    configuration = Empty.build(configuration);
                    Annotations.build(configuration);

                    String callee=null;
                    for (StackTraceElement stackTraceElement : Iterables.cycle(Thread.currentThread().getStackTrace())) {
                        final String className = stackTraceElement.getClassName();
                        if (!className.startsWith("org.reflections") && !className.startsWith("java.lang")
                                || className.startsWith("org.relflections.tests") /*ugh*/) {
                            callee = className;
                            break;
                        }
                    }

                    return configuration
                            .addExcludedFqnsPrefixes("java", "javax", "sun", "com.sun")
                            .setScanSources(true)
                            .addUrls(ClasspathHelper.getUrlForClassName(callee));
                }
            },
    Runtime
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .setFetchPostCompiledResources(false)
                            .addUrls(ClasspathHelper.getUrlsForCurrentClasspath());
                }},
    Development
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addUrls(ClasspathHelper.getUrlsForSourcesOnly())
                            .setScanSources(true);
                }},
    Full
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addElementTypesToScan(ElementTypes.all())
                            .addInvertedElementTypes(ElementTypes.all())
                            .setFetchPostCompiledResources(true);
                }},
    ////
    PostCompiled
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .setFetchPostCompiledResources(true);
                }},
    PreCompiledAndSources
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addUrls(ClasspathHelper.getUrlsForCurrentClasspath())
                            .setFetchPostCompiledResources(true);
                }},

    ////
    Transitive
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration.setComputeTransitiveClosure(true);
                }},
    Annotations
            {
                @Override
                public Configuration build(Configuration configuration) {
                    return configuration
                            .addElementTypesToScan(annotations)
                            .addInvertedElementTypes(annotations);
                }
            };

    ////
//    public static class PreCompiled /*implementss Builder*/ {
//        protected final String preCompiledPattern;
//
//        public PreCompiled(String preCompiledPattern) {this.preCompiledPattern = preCompiledPattern;}
//
//        public Configuration build(Configuration configuration) {
//            return configuration
//                    .addPreCompiledResources(
//                            ClasspathHelper.getPreCompiledResources(preCompiledPattern));
//        }
//    }

//    public static class PreCompiledAndSources extends PreCompiled {
//        public PreCompiledAndSources(String preCompiledPattern) {
//            super(preCompiledPattern);
//        }
//
//        public Configuration build(Configuration configuration) {
//            return configuration
//                    .addPreCompiledResources(
//                            ClasspathHelper.getPreCompiledResources(preCompiledPattern))
//                    .addUrls(ClasspathHelper.getUrlsForSourcesOnly());
//        }
//    }

    ////
    public abstract Configuration build(Configuration configuration);

    public static Configuration build(ConfigurationBuilder... builders) {
        //noinspection deprecation
        final Configuration configuration = Default.build((Configuration) null);
        for (ConfigurationBuilder builder : builders) {
            builder.build(configuration);
        }

        return configuration;
    }
}
