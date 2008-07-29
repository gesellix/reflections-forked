package org.reflections;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClassMD;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.PostCompiled;
import static org.reflections.model.ConfigurationBuilder.Transitive;

import java.util.Collection;

/**
 * @author mamo
 */
@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
public class ReflectionsTest {

    @Test
    public void reflectionsInstance() {

        Reflections reflections = new Reflections() {}
                .setConfiguration(Configuration
                        .build(Transitive)
                        .addUrls(ClasspathHelper.getUrlForClass(getClass())));

        ClassMD c2MD = reflections.getClassMD(ScannerScansAnnotationsCorrectly.TestModel.C2.class);

        Assert.assertNotNull(c2MD);
    }

    @Test
    public void postCompiledResources() {
        Reflections reflections = new Reflections() {}
                .setConfiguration(Configuration
                        .build(PostCompiled)
                        .setPostCompiledResourcesPattern("reflections",".*.xml")
                        .setScanSources(false)
                        .addUrls(ClasspathHelper.getUrlsForCurrentClasspath()));

        final ClasspathMD classpathMD = reflections.classpathMD;
        final ClassMD classMD = classpathMD.getClassMD("C1");
        final Collection<String> strings = classpathMD.getInvertedMD("A1");

        System.out.println("reflections = " + reflections);
    }
}
