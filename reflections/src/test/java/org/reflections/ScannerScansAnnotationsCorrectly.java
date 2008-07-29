package org.reflections;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import static org.reflections.ScannerScansAnnotationsCorrectly.TestModel.*;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.reflections.model.ElementTypes.annotations;
import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;
import static org.reflections.model.ConfigurationBuilder.Empty;
import org.reflections.actors.Scanner;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.ClasspathHelper;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mamo
 */
public class ScannerScansAnnotationsCorrectly {

    @SuppressWarnings({"ALL"})
    //kindly leave it unformatted single lined
    public interface TestModel {
        //interfaceWithOneExplicitAnnotation
        public @Retention(RUNTIME) @interface AI1 {}
        public @AI1 interface I1 {}

        //interfaceWithTwoExplicitAnnotations
        public @Retention(RUNTIME) @interface AI2 {}
        public @AI1 @AI2 interface I12 {}

        //interfaceWithMetaAnnotations
        public @Retention(RUNTIME) @interface MAI3 {}
        public @Retention(RUNTIME) @MAI3 @interface AI3 {}
        public @AI3 interface I3 {}

        //interfaceWithInheritedMetaAnnotations
        public @Retention(RUNTIME) @interface AI4 {}
        public @AI4 interface I4 extends I3 {}

        //classWithInheritedAnnotationsFromInterface
        public @Retention(RUNTIME) @interface AC1 {}
        public @AC1 class C1 implements I4 {}

        //classWithInheritedAnnotationsFromSuperclass
        public class C2 extends C1 {}
    }
    @Test
    public void interfaceWithOneExplicitAnnotationIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I1.class)).getMD(annotations),
                newHashSet(
                        name(AI1.class),
                        name(Retention.class)
                ));
    }

    @Test
    public void interfaceWithTwoExplicitAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I12.class)).getMD(annotations),
                newHashSet(
                        name(AI1.class),
                        name(AI2.class),
                        name(Retention.class)
                ));
    }

    @Test
    public void interfaceWithMetaAnnotationsIsCorrect() {
        Asserts.collectionsContained(
                classpathMD.getClassMD(name(I3.class)).getMD(annotations),
                newHashSet(
                        name(AI3.class),
                        name(MAI3.class),
                        name(Retention.class)

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

    private static ClasspathMD classpathMD;

    @BeforeClass
    public static void scanModel1() {
        //get configuration
        Configuration configuration = Configuration.build(Empty)
                .setComputeTransitiveClosure(true)
                .addUrls(ClasspathHelper.getUrlForClass(ScannerScansAnnotationsCorrectly.class));

        //create classPathMD
        classpathMD = new ClasspathMD();

        //scan all relevant classes
        Scanner scanner = new ClasspathScanner(configuration, classpathMD);
        scanner.scan();

    }

    protected static String name(Class<?> aClass) {return aClass.getName();}

}
