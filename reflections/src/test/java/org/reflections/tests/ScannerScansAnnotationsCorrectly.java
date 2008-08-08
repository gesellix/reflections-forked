package org.reflections.tests;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.Asserts;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.model.ElementTypes.annotations;
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
        public class C2 extends C1 {}
    }

    @Test
    public void interfaceWithOneExplicitAnnotationIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I1.class)).getMD(annotations),
                newHashSet(
                        name(AI1.class)
                ));
    }

    @Test
    public void interfaceWithTwoExplicitAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I12.class)).getMD(annotations),
                newHashSet(
                        name(AI1.class),
                        name(AI2.class)
                ));
    }

    @Test
    public void interfaceWithMetaAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I3.class)).getMD(annotations),
                newHashSet(
                        name(AI3.class),
                        name(MAI3.class)
                ));
    }

    @Test
    public void interfaceWithInheritedMetaAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I4.class)).getMD(annotations),
                concat(
                        newHashSet(name(AI4.class)),
                        classpathMD.getClassMD(name(I3.class)).getMD(annotations)));
    }

    @Test
    public void classWithInheritedAnnotationsFromInterfaceIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(C1.class)).getMD(annotations),
                concat(
                        newHashSet(name(AC1.class)),
                        classpathMD.getClassMD(name(I4.class)).getMD(annotations)));
    }

    @Test public void classWithInheritedAnnotationsFromSuperclassIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(C2.class)).getMD(annotations),
                classpathMD.getClassMD(name(C1.class)).getMD(annotations));
    }

    protected static String name(Class<?> aClass) {return aClass.getName();}

}
