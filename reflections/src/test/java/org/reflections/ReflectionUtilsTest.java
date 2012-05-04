package org.reflections;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.reflections.scanners.FieldAnnotationsScanner;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Predicates.and;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.*;
import static org.reflections.Reflections.*;

/**
 * @author mamo
 */
public class ReflectionUtilsTest {

    @Test
    public void getAllTest() {
        Reflections reflections = new Reflections(new Object[] {TestModel.class}, new FieldAnnotationsScanner());

        Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(TestModel.AF1.class);
        Set<? extends Field> allFields = getAll(annotatedFields, withModifier(Modifier.PROTECTED));
        assertTrue(allFields.size() == 1);
        assertTrue(allFields.iterator().next().getName().equals("f2"));
    }

    @Test public void getAllWith() {
        //Reflections.getAllXXX(Class, withYYY)
        //Reflections.getAllXXX(Class, Predicates.and(withYYY(..), withZZZ(..), ...)

        Predicate<Method> getterMethod = and(withModifier(Modifier.PUBLIC), withPrefix("get"), withParametersCount(0));

        assertThat(getAllMethods(Object.class, getterMethod), names("getClass"));
        assertThat(getAllMethods(Arrays.<Class<?>>asList(Object.class), getterMethod), names("getClass"));

        //
        Set<Method> returnMember = getAllMethods(Class.class, withReturnTypeAssignableTo(Member.class));
        Set<Method> returnsAssignableToMember = getAllMethods(Class.class, withReturnType(Method.class));

        assertTrue(returnMember.containsAll(returnsAssignableToMember));
        assertFalse(returnsAssignableToMember.containsAll(returnMember));

        returnsAssignableToMember = getAllMethods(Class.class, withReturnType(Field.class));
        assertTrue(returnMember.containsAll(returnsAssignableToMember));
        assertFalse(returnsAssignableToMember.containsAll(returnMember));

        //
        assertThat(getAllFields(TestModel.C4.class, withName("f1")), names("f1"));

        assertThat(getAllFields(TestModel.C4.class, withAnnotation(TestModel.AF1.class)), names("f1", "f2"));

        assertThat(getAllFields(TestModel.C4.class, withAnnotation(new TestModel.AF1() {
                            public String value() {return "2";}
                            public Class<? extends Annotation> annotationType() {return TestModel.AF1.class;}})),
                names("f2"));

        assertThat(getAllFields(TestModel.C4.class, withTypeAssignableTo(String.class)), names("f1", "f2", "f3"));

        //todo
        Set<Method> m2 = getAllMethods(TestModel.C4.class, withParametersAssignableTo(int.class, String.class));
        Set<Method> m3 = getAllMethods(TestModel.C4.class, withParametersAssignableTo(int.class, String[].class));
        Set<Method> m4 = getAllMethods(TestModel.C4.class, withParametersAssignableTo(int.class, Object.class));

        Set<Method> m5 = getAllMethods(TestModel.C4.class, withReturnType(String.class));
        Set<Method> m6 = getAllMethods(TestModel.C4.class, withReturnTypeAssignableTo(String.class));

    }

    private BaseMatcher<Set<? extends Member>> names(final String... namesArray) {
        return new BaseMatcher<Set<? extends Member>>() {

                public boolean matches(Object o) {
                    Collection<String> transform = Collections2.transform((Set<Member>) o, new Function<Member, String>() {
                        public String apply(@Nullable Member input) {
                            return input.getName();
                        }
                    });
                    final Collection<?> names = Arrays.asList(namesArray);
                    return transform.containsAll(names) && names.containsAll(transform);
                }

                public void describeTo(Description description) {
                }
            };
    }

    @Retention(RUNTIME) @interface Event {
        String value() default "";
    }
    @Retention(RUNTIME) @interface Mark {}

    class C4 {
        void onEvent(@Event String key) {}
        @Mark void onEvent1(@Event String key, @Event("aaa") String value) {}
        void onEvent2(String key) {}
    }

    @Test public void testWithParameterAnnotations() {
        assertThat(getAllMethods(C4.class, withParameterAnnotations(Event.class)), names("onEvent"));
        assertThat(getAllMethods(C4.class, withParameterAnnotations(Event.class, Event.class)), names("onEvent1"));
        assertThat(getAllMethods(C4.class, and(withAnnotation(Mark.class), withParameterAnnotations(Event.class, Event.class))), names("onEvent1"));
        assertThat(getAllMethods(C4.class, withParameterAnnotations(
                new Event() {
                    public String value() { return ""; }
                    public Class<? extends Annotation> annotationType() { return Event.class; }
                },
                new Event() {
                    public String value() { return "aaa"; }
                    public Class<? extends Annotation> annotationType() { return Event.class; }
                }
        )), names("onEvent1"));
    }
}
