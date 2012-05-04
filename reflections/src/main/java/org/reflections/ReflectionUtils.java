package org.reflections;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.reflections.util.ClasspathHelper;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

/** convenient java reflection helper methods
 * <p>
 *     1. some helper methods to get type by name: {@link #forName(String, ClassLoader...)} and {@link #forNames(Iterable, ClassLoader...)}
 * <p>
 *     2. some helper methods to get all types/methods/fields matching some predicates, generally:
 *     <pre> Set&#60?> result = getAllXXX(type/s, withYYY) </pre>
 *     <p>where get methods are:
 *     <ul>
 *         <li>{@link #getAllSuperTypes(Class, com.google.common.base.Predicate)}
 *         <li>{@link #getAllFields(Class, com.google.common.base.Predicate)}
 *         <li>{@link #getAllMethods(Class, com.google.common.base.Predicate)}
 *         <li>{@link #getAll(Iterable, com.google.common.base.Predicate)}
 *     </ul>
 *     <p>and predicates included here all starts with "with", such as 
 *     <ul>
 *         <li>{@link #withAnnotation(java.lang.annotation.Annotation)}
 *         <li>{@link #withModifier(int)}
 *         <li>{@link #withName(String)}
 *         <li>{@link #withParameters(Class[])}
 *         <li>{@link #withParameterAnnotations(Class[])} 
 *         <li>{@link #withParametersAssignableTo(Class[])}
 *         <li>{@link #withPrefix(String)}
 *         <li>{@link #withReturnType(Class)}
 *         <li>{@link #withType(Class)}
 *         <li>{@link #withTypeAssignableTo}
 *     </ul> 
 *
 *     <p><br>
 *      for example, getting all getters would be:
 *     <pre>
 *      Set&#60Method> getters = getAllMethods(someClasses, 
 *              Predicates.and(
 *                      withModifier(Modifier.PUBLIC), 
 *                      withPrefix("get"), 
 *                      withParametersCount(0)));
 *     </pre>
 * */
public abstract class ReflectionUtils {

    //primitive parallel arrays 
    private static List<String> primitiveNames;
    private static List<Class> primitiveTypes;
    private static List<String> primitiveDescriptors;

    /** get all super types of given {@code type}, including, filtered by optional {@code predicates}
     * <p>for example:
     * <pre>
     * Set&#60Class&#60?>> annotatedSuperTypes =
     *      getAllSuperTypes(type, withAnnotation(annotation));
     * </pre>*/
    public static Set<Class<?>> getAllSuperTypes(final Class<?> type, Predicate<? super Class<?>> predicate) {
        Set<Class<?>> result = Sets.newHashSet();
        if (type != null) {
            result.add(type);
            result.addAll(getAllSuperTypes(type.getSuperclass(), Predicates.alwaysTrue()));
            for (Class<?> inter : type.getInterfaces()) {
                result.addAll(getAllSuperTypes(inter, Predicates.alwaysTrue()));
            }
        }

        return ImmutableSet.copyOf(Collections2.filter(result, predicate));
    }

    public static Set<Class<?>> getAllSuperTypes(final Iterable<? extends Class<?>> types, Predicate<? super Class<?>> predicate) {
        Set<Class<?>> result = Sets.newHashSet(); for (Class<?> type : types) result.addAll(getAllSuperTypes(type, predicate));
        return result;
    }

    /** get all fields of given {@code type}, including, filtered by {@code predicate}
     * <p>for example:
     * <pre>
     * Set&#60Field&#60?>> injectables =
     *      getAllFields(type,
     *          Predicates.or(withAnnotation(Inject.class), withAnnotation(Autowired.class)));
     * </pre>*/
    public static Set<Field> getAllFields(final Class<?> type, Predicate<? super Field> predicate) {
        Set<Field> result = Sets.newHashSet();
        for (Class<?> t : getAllSuperTypes(type, Predicates.alwaysTrue())) Collections.addAll(result, t.getDeclaredFields());

        return ImmutableSet.copyOf(Collections2.filter(result, predicate));
    }

    public static Set<Field> getAllFields(final Iterable<? extends Class<?>> types, Predicate<? super Field> predicate) {
        Set<Field> result = Sets.newHashSet(); for (Class<?> type : types) result.addAll(getAllFields(type, predicate));
        return result;
    }

    /** get all methods of given {@code type} filtered by {@code predicate}
     * <p>for example:
     * <pre>
     * Set&#60Method> getters =
     *      getAllMethods(someClasses,
     *          Predicates.and(
     *              withModifier(Modifier.PUBLIC),
     *              withPrefix("get"),
     *              withParametersCount(0)));
     * </pre>*/
    public static Set<Method> getAllMethods(final Class<?> type, Predicate<? super Method> predicate) {
        Set<Method> result = Sets.newHashSet();
        for (Class<?> t : getAllSuperTypes(type, Predicates.alwaysTrue())) {
            Collections.addAll(result, t.isInterface() ? t.getMethods() : t.getDeclaredMethods());
        }

        return ImmutableSet.copyOf(Collections2.filter(result, predicate));
    }

    /** get all methods of given {@code types}, filtered by {@code predicate}*/
    public static Set<Method> getAllMethods(final Iterable<? extends Class<?>> types, Predicate<? super Method> predicate) {
        Set<Method> result = Sets.newHashSet(); for (Class<?> type : types) result.addAll(getAllMethods(type, predicate));

        return ImmutableSet.copyOf(result);
    }

    /** filter all given {@code elements} with {@code predicate} */
    public static <T extends AnnotatedElement> Set<T> getAll(final Iterable<? extends T> elements, Predicate<? super T> predicate) {
        return ImmutableSet.copyOf(Iterables.filter(elements, predicate));
    }

    /** where member name equals given {@code name} */
    public static <T extends Member> Predicate<T> withName(final String name) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && input.getName().equals(name);
            }
        };
    }

    /** where member name startsWith given {@code prefix} */
    public static <T extends Member> Predicate<T> withPrefix(final String prefix) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && input.getName().startsWith(prefix);
            }
        };
    }

    /** where element is annotated with given {@code annotation} */
    public static <T extends AnnotatedElement> Predicate<T> withAnnotation(final Class<? extends Annotation> annotation) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && input.isAnnotationPresent(annotation);
            }
        };
    }

    /** where element is annotated with given {@code annotations} */
    public static <T extends AnnotatedElement> Predicate<T> withAnnotations(final Class<? extends Annotation>... annotations) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && Arrays.equals(annotations, input.getAnnotations());
            }
        };
    }

    /** where element is annotated with given {@code annotation}, including member matching */
    public static <T extends AnnotatedElement> Predicate<T> withAnnotation(final Annotation annotation) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && input.isAnnotationPresent(annotation.annotationType()) &&
                        areAnnotationMembersMatching(input.getAnnotation(annotation.annotationType()), annotation);
            }
        };
    }

    /** where element is annotated with given {@code annotations}, including member matching */
    public static <T extends AnnotatedElement> Predicate<T> withAnnotations(final Annotation... annotations) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                if (input != null) {
                    Annotation[] inputAnnotations = input.getAnnotations();
                    if (inputAnnotations.length == annotations.length) {
                        for (int i = 0; i < inputAnnotations.length; i++) {
                            if (!areAnnotationMembersMatching(inputAnnotations[i], annotations[i])) return false;
                        }
                    }
                    
                }
                return true;
            }
        };
    }

    /** when method parameter types equals given {@code types} */
    public static Predicate<Method> withParameters(final Class<?>... types) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                return input != null && Arrays.equals(input.getParameterTypes(), types);
            }
        };
    }

    /** when method parameter types assignable to given {@code types} */
    public static Predicate<Method> withParametersAssignableTo(final Class... types) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                if (input != null) {
                    Class<?>[] parameterTypes = input.getParameterTypes();
                    if (parameterTypes.length == types.length) {
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (!types[i].isAssignableFrom(parameterTypes[i])) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /** when method parameter types count equal given {@code count} */
    public static Predicate<Method> withParametersCount(final int count) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                return input != null && input.getParameterTypes().length == count;
            }
        };
    }

    /** when method parameter annotations matches given {@code annotations} */
    public static Predicate<Method> withParameterAnnotations(final Annotation... annotations) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                if (input != null && annotations != null) {
                    Annotation[][] parameterAnnotations = input.getParameterAnnotations();
                    if (annotations.length == parameterAnnotations.length) {
                        for (int i = 0; i < parameterAnnotations.length; i++) {
                            boolean any = false;
                            for (Annotation annotation : parameterAnnotations[i]) {
                                if (areAnnotationMembersMatching(annotations[i], annotation)) { any = true; break; }
                            }
                            if (!any) return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /** when method parameter annotations matches given {@code annotations}, including member matching */
    public static Predicate<Method> withParameterAnnotations(final Class<? extends Annotation>... annotationClasses) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                if (input != null && annotationClasses != null) {
                    Annotation[][] parameterAnnotations = input.getParameterAnnotations();
                    if (annotationClasses.length == parameterAnnotations.length) {
                        for (int i = 0; i < parameterAnnotations.length; i++) {
                            boolean any = false;
                            for (Annotation annotation : parameterAnnotations[i]) {
                                if (annotationClasses[i].equals(annotation.annotationType())) { any = true; break; }
                            }
                            if (!any) return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /** when field type equal given {@code type} */
    public static <T> Predicate<Field> withType(final Class<T> type) {
        return new Predicate<Field>() {
            public boolean apply(@Nullable Field input) {
                return input != null && input.getType().equals(type);
            }
        };
    }

    /** when field type assignable to given {@code type} */
    public static <T> Predicate<Field> withTypeAssignableTo(final Class<T> type) {
        return new Predicate<Field>() {
            public boolean apply(@Nullable Field input) {
                return input != null && type.isAssignableFrom(input.getType());
            }
        };
    }

    /** when method return type equal given {@code type} */
    public static <T> Predicate<Method> withReturnType(final Class<T> type) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                return input != null && input.getReturnType().equals(type);
            }
        };
    }

    /** when method return type assignable from given {@code type} */
    public static <T> Predicate<Method> withReturnTypeAssignableTo(final Class<T> type) {
        return new Predicate<Method>() {
            public boolean apply(@Nullable Method input) {
                return input != null && type.isAssignableFrom(input.getReturnType());
            }
        };
    }

    /** when member modifier matches given {@code mod}
     * <p>for example:
     * <pre>
     * withModifier(Modifier.PUBLIC)
     * withModifier(Modifier.PROTECTED | Modifier.PUBLIC)
     * </pre>
     */
    public static <T extends Member> Predicate<T> withModifier(final int mod) {
        return new Predicate<T>() {
            public boolean apply(@Nullable T input) {
                return input != null && (input.getModifiers() & mod) != 0;
            }
        };
    }

    /** checks for annotation member values matching, based on equality of members */
    public static boolean areAnnotationMembersMatching(Annotation annotation1, Annotation annotation2) {
        if (annotation2 != null && annotation1.annotationType() == annotation2.annotationType()) {
            for (Method method : annotation1.annotationType().getDeclaredMethods()) {
                try {
                    if (!method.invoke(annotation1).equals(method.invoke(annotation2))) {
                        return false;
                    }
                } catch (Exception e) {
                    throw new ReflectionsException(String.format("could not invoke method %s on annotation %s", method.getName(), annotation1.annotationType()), e);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * returns a subset of given annotatedWith, where annotation member values matches the given annotation
     */
    public static <T extends AnnotatedElement> Set<T> getMatchingAnnotations(final Set<T> annotatedElements, final Annotation annotation) {
        Set<T> result = Sets.newHashSet();

        for (T annotatedElement : annotatedElements) {
            Annotation annotation1 = annotatedElement.getAnnotation(annotation.annotationType());
            if (areAnnotationMembersMatching(annotation, annotation1)) {
                result.add(annotatedElement);
            }
        }

        return result;
    }

    /** tries to resolve a java type name to a Class
     * <p>if optional {@link ClassLoader}s are not specified, then both {@link org.reflections.util.ClasspathHelper#contextClassLoader()} and {@link org.reflections.util.ClasspathHelper#staticClassLoader()} are used
     * */
    public static Class<?> forName(String typeName, ClassLoader... classLoaders) {
        if (getPrimitiveNames().contains(typeName)) {
            return getPrimitiveTypes().get(getPrimitiveNames().indexOf(typeName));
        } else {
            String type;
            if (typeName.contains("[")) {
                int i = typeName.indexOf("[");
                type = typeName.substring(0, i);
                String array = typeName.substring(i).replace("]", "");

                if (getPrimitiveNames().contains(type)) {
                    type = getPrimitiveDescriptors().get(getPrimitiveNames().indexOf(type));
                } else {
                    type = "L" + type + ";";
                }

                type = array + type;
            } else {
                type = typeName;
            }

            for (ClassLoader classLoader : ClasspathHelper.classLoaders(classLoaders)) {
                try { return Class.forName(type, false, classLoader); }
                catch (Exception e) { /*continue*/ }
            }

            return null;
        }
    }

    /** try to resolve all given string representation of types to a list of java types */
    public static <T> List<Class<? extends T>> forNames(final Iterable<String> classes, ClassLoader... classLoaders) {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        for (String className : classes) {
            //noinspection unchecked
            result.add((Class<? extends T>) forName(className, classLoaders));
        }
        return result;
    }

    public static List<String> names(Iterable<Class<?>> types) {
        List<String> result = Lists.newArrayList();
        for (Class<?> type : types) {
            result.add(type.getName());
        }
        return result;
    }

    public static List<String> getPrimitiveNames() {
        return primitiveNames == null ? (primitiveNames = Lists.newArrayList("boolean", "char", "byte", "short", "int", "long", "float", "double", "void")) : primitiveNames;
    }

    public static List<Class> getPrimitiveTypes() {
        return primitiveTypes == null ? (primitiveTypes = Lists.<Class>newArrayList(boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class, void.class)) : primitiveTypes;
    }

    public static List<String> getPrimitiveDescriptors() {
        return primitiveDescriptors == null ? (primitiveDescriptors = Lists.newArrayList("Z", "C", "B", "S", "I", "J", "F", "D", "V")) : primitiveDescriptors;
    }
}
