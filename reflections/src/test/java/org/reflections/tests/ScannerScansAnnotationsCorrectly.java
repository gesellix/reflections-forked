package org.reflections.tests;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.Asserts;
import org.reflections.helper.ClasspathHelper;
import static org.reflections.helper.DescriptorHelper.className;
import static org.reflections.helper.TestHelper.toTypes;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.tests.ScannerScansAnnotationsCorrectly.TestModel.*;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mamo
 */
public class ScannerScansAnnotationsCorrectly {
    private static ClasspathMD classpathMD = new ClasspathMD();

    @BeforeClass
    public static void scanTestModel() {
        new ClasspathScanner(
                Configuration.build
                        (Annotations, Transitive, ExcludeAll, ThisUrl)
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(ScannerScansAnnotationsCorrectly.TestModel.class))
                , classpathMD).scan();
    }

    @SuppressWarnings({"ALL"})
    //kindly leave it unformatted single lined
    public interface TestModel {
        public @Retention(RUNTIME) @interface AI1 {}
        public @AI1 interface I1 {}
        public @Retention(RUNTIME) @interface AI2 {}
        public @AI1 @AI2 interface I12 {}
        public @Retention(RUNTIME) @interface MAI3 {}
        public @Retention(RUNTIME) @MAI3 @interface AI3 {}
        public @AI3 interface I3 {}
        public @Retention(RUNTIME) @interface AI4 {}
        public @AI4 interface I4 extends I3 {}
        public @Retention(RUNTIME) @interface AC1 {}
        public @AC1 class C1 implements I4 {}
        public @Retention(RUNTIME) @interface AC2 {}
        public @AC2 class C2 extends C1 {}
    }

    @Test
    public void interfaceWithOneExplicitAnnotationIsCorrect() {
        Asserts.collectionsContained(
                toTypes(classpathMD.getType(className(I1.class)).getAnnotations()),
                newHashSet(className(AI1.class)
        ));
    }

    @Test
    public void interfaceWithTwoExplicitAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                toTypes(classpathMD.getType(className(I12.class)).getAnnotations()),
                newHashSet(
                        className(AI1.class),
                        className(AI2.class)
                ));
    }

    @Test
    public void interfaceWithMetaAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                toTypes(classpathMD.getType(className(I3.class)).getAnnotations()),
                newHashSet(
                        className(AI3.class),
                        className(MAI3.class)
                ));
    }

    @Test
    public void interfaceWithInheritedMetaAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                toTypes(classpathMD.getType(className(I4.class)).getAnnotations()),
                concat(
                        newHashSet(className(AI4.class)),
                        toTypes(classpathMD.getType(className(I3.class)).getAnnotations())));
    }

    @Test
    public void classWithInheritedAnnotationsFromInterfaceIsCorrect() {
        Asserts.collectionsContained(
                toTypes(classpathMD.getType(className(C1.class)).getAnnotations()),
                concat(
                        newHashSet(className(AC1.class)),
                        toTypes(classpathMD.getType(className(I4.class)).getAnnotations())));
    }

    @Test
    public void classWithInheritedAnnotationsFromSuperclassIsCorrect() {
        Assert.assertTrue(
            classpathMD.getType(className(C2.class)).getAnnotations()
                    .containsAll(classpathMD.getType(className(C1.class)).getAnnotations()));
    }
}
