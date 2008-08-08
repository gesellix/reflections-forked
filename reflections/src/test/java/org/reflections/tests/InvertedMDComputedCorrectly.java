package org.reflections.tests;

import static com.google.common.collect.Sets.newHashSet;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.helper.Asserts;
import org.reflections.helper.ClasspathHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.*;
import static org.reflections.tests.InvertedMDComputedCorrectly.TestModel.*;

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
        ClasspathMD classpathMD = new ClasspathMD();
        new ClasspathScanner(
                Configuration.build
                        (Annotations, ExcludeAll, ThisUrl)
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(InvertedMDComputedCorrectly.TestModel.class))
                , classpathMD).scan();

        Asserts.collectionsContained(
                classpathMD.getInvertedMD(name(AI1.class)),
                newHashSet(
                        name(I1.class), //interface with explicit annotation
                        name(I2.class), //another interface with explicit annotation
                        name(C1.class))); //class with explicit annotation
    }

    @Test
    public void invertedAnnotationWhenTransitiveClosureComputerIsCorrect() {
        ClasspathMD classpathMD = new ClasspathMD();
        new ClasspathScanner(
                Configuration.build
                        (Annotations, Transitive, ExcludeAll, ThisUrl)
                        .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(InvertedMDComputedCorrectly.TestModel.class))
                , classpathMD).scan();

        Asserts.collectionsContained(
                classpathMD.getInvertedMD(name(AI1.class)),
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
