package org.reflections.tests;

import static com.google.common.collect.Sets.newHashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.actors.impl.ClasspathScanner;
import org.reflections.actors.impl.QueryDSL;
import org.reflections.helper.Asserts;
import org.reflections.helper.ClasspathHelper;
import static org.reflections.helper.DescriptorHelper.className;
import static org.reflections.helper.TestHelper.toTypes;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import static org.reflections.model.ConfigurationBuilder.*;
import org.reflections.model.ElementTypes;
import org.reflections.model.meta.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.util.Set;

/**
 * @author mamo
 */
public class ReflectionQueryIsCorrect {

    private static QueryDSL queryDSL;
    private static ClasspathMD classpathMD = new ClasspathMD();

    @BeforeClass
    public static void scanTestModel() {
        final Configuration configuration = Configuration
                .build(ExcludeAll, Transitive, ThisUrl)
                .addIncludePatterns(ClasspathHelper.getPatternForFqnPrefix(TestModel.class))
                .addElementTypesToScan(ElementTypes.all())
                .addReverseElementTypes(ElementTypes.all());

        new ClasspathScanner(configuration, classpathMD).scan();

        queryDSL = new QueryDSL(classpathMD);
    }

    @SuppressWarnings({"ALL"})
    //kindly leave it unformatted single lined
    public interface TestModel {
        public @Retention(RUNTIME) @interface MAI3 {}
        public @Retention(RUNTIME) @MAI3 @interface AI3 {}
        public @AI3 interface I3 {}
        public @Retention(RUNTIME) @interface AI4 {}
        public @AI4 interface I4 extends I3 {}
        public @Retention(RUNTIME) @interface AC1 {}
        public @AC1 class C1 implements I4 {}
        public @Retention(RUNTIME) @interface AC2 {
            String value();
        }
        public @AC2("ugh?!") class C2 extends C1 {}
        public @AC2("grr...") class C3 extends C1 {}
        public @Retention(RUNTIME) @interface AM1 {
            String value();
        }
        public @Retention(RUNTIME) @interface AF1 {
            String value();
        }
        public class C4 {
            @AF1("1") private String f1;
            @AF1("2") protected String f2;

            @AM1("1") public void m1() {}
            @AM1("1") public void m2(String string) {}
            @AM1("2") public String m3() {return null;}
        }
    }


    @Test
    public void queryTypesAnnotatedWithIsCorrect() {
        //
        Set<MetaInterface> ai3Interfaces = queryDSL
                .select(MetaInterface.class)
                .annotatedWith(TestModel.MAI3.class)
                .query();

        Asserts.collectionsContained(toTypes(ai3Interfaces),
                newHashSet(
                        className(TestModel.I3.class),
                        className(TestModel.I4.class)));

        //
        Set<MetaClass> ai3Classes = queryDSL
                .select(MetaClass.class)
                .annotatedWith(TestModel.MAI3.class)
                .query();

        Asserts.collectionsContained(toTypes(ai3Classes),
                newHashSet(
                        className(TestModel.C1.class),
                        className(TestModel.C2.class),
                        className(TestModel.C3.class)));
    }

    @Test
    public void quertTypesAnnotatedWithInstanceIsCorrect() {
        final Set<MetaClass> ac2Classes = queryDSL
                .select(MetaClass.class)
                .annotatedWith(new TestModel.AC2() {
                    public String value() {return "ugh?!";}
                    public Class<? extends Annotation> annotationType() {return TestModel.AC2.class;}})
                .query();

        Asserts.collectionsContained(
                toTypes(ac2Classes),
                newHashSet(
                        className(TestModel.C2.class)));
    }

    @Test
    public void queryTypesOfTypeIsCorrect() {
        final Set<MetaClass> i3Classes = queryDSL
                .select(MetaClass.class)
                .ofType(TestModel.I3.class)
                .query();

        Asserts.collectionsContained(toTypes(i3Classes),
                newHashSet(
                        className(TestModel.C1.class),
                        className(TestModel.C2.class),
                        className(TestModel.C3.class)));

        final Set<MetaInterface> i3Intefaces = queryDSL
                .select(MetaInterface.class)
                .ofType(TestModel.I3.class)
                .query();

        Asserts.collectionsContained(toTypes(i3Intefaces),
                newHashSet(
                        className(TestModel.I4.class)));
    }

    @Test
    public void queryTypesOfTypeAndAnnotationIsCorrect() {
        final Set<MetaClass> i3Ac2Classes = queryDSL
                .select(MetaClass.class)
                .ofType(TestModel.I3.class)
                .annotatedWith(TestModel.AC2.class)
                .query();

        Asserts.collectionsContained(
                toTypes(i3Ac2Classes),
                newHashSet(
                        className(TestModel.C2.class),
                        className(TestModel.C3.class)));

        final Set<MetaClass> c1Classes = queryDSL
                .select(MetaClass.class)
                .ofType(TestModel.C1.class)
                .annotatedWith(new TestModel.AC2() {
                    public String value() {return "ugh?!";}
                    public Class<? extends Annotation> annotationType() {return TestModel.AC2.class;}})
                .query();

        Asserts.collectionsContained(
                toTypes(c1Classes),
                newHashSet(
                        className(TestModel.C2.class)));
    }

    @Test
    public void queryMethodsAnnotatedWithIsCorrect() {
        final Set<MetaMethod> am1Methods = queryDSL
                .select(MetaMethod.class)
                .annotatedWith(TestModel.AM1.class)
                .query();

        Asserts.collectionsContained(
                am1Methods,
                newHashSet(
                        classpathMD.getType(className(TestModel.C4.class)).getMethod("m1"),
                        classpathMD.getType(className(TestModel.C4.class)).getMethod("m2"),
                        classpathMD.getType(className(TestModel.C4.class)).getMethod("m3")));

        final Set<MetaMethod> am1_1Methods = queryDSL
                .select(MetaMethod.class)
                .annotatedWith(new TestModel.AM1() {
                    public String value() {return "1";}
                    public Class<? extends Annotation> annotationType() {return TestModel.AM1.class;}})
                .query();

        Asserts.collectionsContained(
                am1_1Methods,
                newHashSet(
                        classpathMD.getType(className(TestModel.C4.class)).getMethod("m1"),
                        classpathMD.getType(className(TestModel.C4.class)).getMethod("m2")));
    }

    @Test
    public void queryFieldAnnotatedWithIsCorrect() {
        final Set<MetaField> af1classes = queryDSL
                .select(MetaField.class)
                .annotatedWith(TestModel.AF1.class)
                .query();

        Asserts.collectionsContained(
                af1classes,
                newHashSet(
                        ((MetaClass) classpathMD.getType(className(TestModel.C4.class))).getField("f1"),
                        ((MetaClass) classpathMD.getType(className(TestModel.C4.class))).getField("f2")));

        final Set<MetaField> af1_1classes = queryDSL
                .select(MetaField.class)
                .annotatedWith(new TestModel.AF1() {
                    public String value() {return "2";}
                    public Class<? extends Annotation> annotationType() {return TestModel.AF1.class;}})
                .query();

        Asserts.collectionsContained(
                af1_1classes,
                newHashSet(
                        ((MetaClass) classpathMD.getType(className(TestModel.C4.class))).getField("f2")));
    }

//    @Test
//    public void queryMemberUsagesDontWorkYet() {
//        final Set<MetaMethod> allMethods = queryDSL
//                .select(MetaMethod.class)
//                .query();
//
//        final Set<MetaMethod> methodsThatReturnString = queryDSL
//                .select(MetaMethod.class)
//                .ofType(String.class)
//                .query();
//
//        System.out.println("methodsThatReturnString = " + methodsThatReturnString);
//    }
}
