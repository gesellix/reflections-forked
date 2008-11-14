package org.reflections.refactor;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.filters.IncludeExcludeChain;
import org.reflections.filters.IncludePrefix;
import org.reflections.filters.PatternFilter;
import org.reflections.scanners.*;
import org.reflections.util.AbstractConfiguration;
import org.reflections.util.ClasspathHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public class ReflectionsTest {

    @SuppressWarnings({"unchecked"})
    private static class TestConfiguration extends AbstractConfiguration {
        {
            setScanners(
                    new SubTypesScanner(),
                    new ClassAnnotationsScanner(),
                    new FieldAnnotationsScanner(),
                    new MethodAnnotationsScanner(),
                    new MethodParametersAnnotationsScanner(),
                    new ConvertersScanner(TestModel.C2.class, TestModel.C3.class));

            setUrls(Arrays.asList(ClasspathHelper.getUrlForClass(TestModel.class)));

            setFilter(new IncludeExcludeChain(
                    new IncludePrefix(TestModel.class.getName())));
        }
    }

    @Test
    public void query() {
        Reflections reflections = new Reflections(new TestConfiguration());
        Assert.assertTrue(
                collectionsContained(
                        reflections.getSubTypesOf(TestModel.I1.class),
                        Arrays.<Class<?>>asList(
                                TestModel.I2.class,
                                TestModel.C1.class,
                                TestModel.C2.class,
                                TestModel.C3.class
                        )));

        Assert.assertTrue(
                collectionsContained(
                        reflections.getTypesAnnotatedWith(TestModel.MAI1.class),
                        Arrays.<Class<?>>asList(
                                TestModel.AI1.class,
                                TestModel.I1.class,
                                TestModel.I2.class,
                                TestModel.C1.class,
                                TestModel.C2.class,
                                TestModel.C3.class
                        )));

        Assert.assertTrue(
                collectionsContained(
                        reflections.getTypesAnnotatedWith(TestModel.AI1.class),
                        Arrays.<Class<?>>asList(
                                TestModel.I1.class,
                                TestModel.I2.class,
                                TestModel.C1.class,
                                TestModel.C2.class,
                                TestModel.C3.class
                        )));

        Assert.assertTrue(
                collectionsContained(
                        reflections.getTypesAnnotatedWith(
                                new TestModel.AC2() {
                                    public String value() {return "ugh?!";}
                                    public Class<? extends Annotation> annotationType() {return TestModel.AC2.class;}
                                }),
                        Arrays.<Class<?>>asList(
                                TestModel.C2.class
                        )));

        try {
            Assert.assertTrue(
                collectionsContained(
                        reflections.getMethodsAnnotatedWith(TestModel.AM1.class),
                        Arrays.<Method>asList(
                                TestModel.C4.class.getMethod("m1"),
                                TestModel.C4.class.getMethod("m2", int.class, String[].class),
                                TestModel.C4.class.getMethod("m3")
                        )));
        } catch (NoSuchMethodException e) {
            Assert.fail();
        }

        try {
            Assert.assertTrue(
                collectionsContained(
                        reflections.getMethodsAnnotatedWith(new TestModel.AM1() {
                            public String value() {return "1";}
                            public Class<? extends Annotation> annotationType() {return TestModel.AM1.class;}
                        }),
                        Arrays.<Method>asList(
                                TestModel.C4.class.getMethod("m1"),
                                TestModel.C4.class.getMethod("m2", int.class, String[].class)
                        )));
        } catch (NoSuchMethodException e) {
            Assert.fail();
        }

        try {
            Assert.assertTrue(
                collectionsContained(
                        reflections.getConverters(TestModel.C2.class, TestModel.C3.class),
                        Arrays.<Method>asList(
                                TestModel.C4.class.getMethod("c2toC3", TestModel.C2.class)
                        )));
        } catch (NoSuchMethodException e) {
            Assert.fail();
        }
    }

    @Test
    public void collect() {
        new Reflections(ClasspathHelper.getUrlsForPackagePrefix("META-INF/reflections"),
                new PatternFilter("META-INF/reflections/.*\\-reflections.xml"));

        //well, not a test really...
    }

    @Test
    public void save() {
        Reflections reflections = new Reflections(new TestConfiguration());
        final String destination = System.getProperty("user.dir") + "/reflections/target/classes/META-INF/reflections/" + getClass().getSimpleName() + ".xml";

        reflections.save(destination);
    }

    //
    private static <T> boolean collectionsContained(Collection<T> c1, Collection<T> c2) {
        return c1.containsAll(c2) && c2.containsAll(c1);
    }

}
