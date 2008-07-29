package org.reflections;

import static com.google.common.collect.Sets.newHashSet;
import static org.reflections.InvertedMDComputedCorrectly.TestModel.*;
import org.junit.Test;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.Configuration;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author mamo
 */

@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
public class InvertedMDComputedCorrectly {

    @SuppressWarnings({"ALL"})
    //kindly leave it unformatted single lined
    public interface TestModel {
        public @Retention(RUNTIME) @interface AI1 {}
        @AI1 public interface I1 {}
        @AI1 public interface I2 {}
        @AI1 public class C1 {}
        public class C2 extends C1 {}
        public class C3 implements I1 {}
    }
    @Test
    public void invertedAnnotationOnTypesIsCorrect() {
        Reflections reflections = new Reflections() {}
                .setConfiguration(Configuration
                        .build()
                        .addUrls(ClasspathHelper.getUrlForClass(InvertedMDComputedCorrectly.class)));

        Asserts.collectionsContained(
                reflections.classpathMD.getInvertedMD(name(AI1.class)),
                newHashSet(
                        name(I1.class), //interface with explicit annotation
                        name(I2.class), //another interface with explicit annotation
                        name(C1.class))); //class with explicit annotation
    }

    @Test
    public void invertedAnnotationWhenTransitiveClosureComputerIsCorrect() {
        Reflections reflections = new Reflections() {}
                .setConfiguration(Configuration
                        .build()
                        .addUrls(ClasspathHelper.getUrlForClass(InvertedMDComputedCorrectly.class))
                        .setComputeTransitiveClosure(true));

        Asserts.collectionsContained(
                reflections.classpathMD.getInvertedMD(name(AI1.class)),
                newHashSet(
                        name(I1.class), //interface with explicit annotation
                        name(I2.class), //another interface with explicit annotation
                        name(C1.class), //class with explicit annotation
                        name(C2.class), //class with implicit annotation from supertype
                        name(C3.class)  //class with implicit annotation from interface
                ));
    }

    //
    protected static String name(Class<?> aClass) {return aClass.getName();}
}
