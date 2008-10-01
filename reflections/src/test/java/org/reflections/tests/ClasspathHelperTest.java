package org.reflections.tests;

import org.junit.Test;
import org.junit.Assert;
import org.reflections.helper.ClasspathHelper;
import org.reflections.helper.DescriptorHelper;
import static org.reflections.helper.ClasspathHelper.getClassLoader;

import java.net.URL;
import java.util.Set;

/**
 * @author mamo
 */
//todo:
public class ClasspathHelperTest {

    @Test
    public void getUrlsForPackagePrefix() {
        String packagePrefix = ClasspathHelperTest.class.getPackage().getName();
        final Set<URL> forPackagePrefix = ClasspathHelper.getUrlsForPackagePrefix(packagePrefix);

        Assert.assertTrue(
                getClassLoader().getResource(DescriptorHelper.qNameToResourceName(packagePrefix))
                .equals(forPackagePrefix.toArray()[0]));
    }

    @Test
    public void getUrlForClass() {
        URL forClass = ClasspathHelper.getUrlForClass(ClasspathHelperTest.class);
        Assert.assertNotNull(forClass);
    }

    @Test
    public void getUrlsForCurrentClasspath() {
        Set<URL> forClass = ClasspathHelper.getUrlsForCurrentClasspath();
        Assert.assertTrue(forClass.size()>7);
    }

    @Test
    public void getUrlsForSourcesOnly() {
        Set<URL> forClass = ClasspathHelper.getUrlsForSourcesOnly();
        Assert.assertFalse(forClass.isEmpty());
    }
}
