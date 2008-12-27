package org.reflections;

import com.thoughtworks.xstream.XStream;
import org.reflections.filters.Filter;
import org.reflections.scanners.*;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;
import org.reflections.util.DescriptorHelper;
import static org.reflections.util.Utils.forNames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Reflections one-stop-shop object.
 * use it to either
 *      scan metadata - according to a given Configuration
 *      or
 *      collect saved xml resources - according to urls and a filter
 *
 * than you can use it for various metadata queries
 */
public class Reflections {
    protected Configuration configuration;
    protected final Store store;

    /**
     * Reflections scan according to configuration
     */
    public Reflections(final Configuration configuration) {
        this.configuration = configuration;
        store = new Store();

        //inject to store instances
        for (Scanner scanner : configuration.getScanners()) {
            scanner.setConfiguration(configuration);
            scanner.setStore(store.get(scanner.getIndexName()));
        }

        scan();
    }

    /**
     * Reflections collect saved xml resources
     */
    public Reflections(final Collection<URL> urls, final Filter<String> resourceNameFilter) {
        store = new Store();
        XStream xStream = new XStream();
        ClassLoader classLoader = Utils.getEffectiveClassLoader();

        for (String resource : ClasspathHelper.getMatchingJarResources(urls, resourceNameFilter)) {
            InputStream stream = classLoader.getResourceAsStream(resource);
            store.merge((Store) xStream.fromXML(stream));
        }
    }

    //
    @SuppressWarnings({"unchecked"})
    protected void scan() {
        Iterable<Object> classesIterator = configuration.getMetadataAdapter().iterateClasses(configuration.getUrls());

        for (Object cls : classesIterator) {
            for (Scanner scanner : configuration.getScanners()) {
                scanner.scan(cls);
            }
        }
    }

    /**
     * merges a Reflections instance into this one
     */
    public void merge(final Reflections reflections) {
        store.merge(reflections.store);
    }

    /**
     * saves the store into a given destination as xml file
     */
    public void save(final String destination) {
        try {
            String xml = new XStream().toXML(store);
            Utils.safeWriteFile(xml, new File(destination));
        } catch (IOException e) {
            throw new RuntimeException(String.format("could not save file %s", destination), e);
        }
    }

    //query

    /**
     * gets all sub types in hierarchy of a given type
     *
     * depends on SubTypesScanner configured, otherwise an empty set is returned
     */
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
        //noinspection RedundantTypeArguments
        return Utils.<T>forNames(getAllSubTypesInHierarchy(type.getName()));
    }

    /**
     * get all types annotated with a given annotation class in hierarchy
     *
     * depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
     */
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<String> result = new HashSet<String>();

        for (String aClass : getAllAnnotatedWithInHierarchy(annotation.getName())) {
            result.add(aClass);
            Set<String> subTypes = getAllSubTypesInHierarchy(aClass);
            if (!subTypes.isEmpty()) {
                result.addAll(subTypes);
            }
        }

        return forNames(result);
    }

    /**
     * get all types annotated with a given annotation instance in hierarchy
     * including annotation member values matching
     *
     * depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
     */
    public Set<Class<?>> getTypesAnnotatedWith(Annotation annotation) {
        Set<Class<?>> result = new HashSet<Class<?>>();

        final Class<? extends Annotation> annotationType = annotation.annotationType();
        final Set<Class<?>> annotatedWith = getTypesAnnotatedWith(annotationType);
        final Map<String, Object> annotationMap = getAnnotationMap(annotation);

        for (Class<?> annotated : annotatedWith) {
            if (annotationMap.equals(getAnnotationMap(annotated.getAnnotation(annotationType)))) {
                result.add(annotated);
            }
        }

        return result;
    }

    /**
     * get all methods annotated with a given annotation class
     *
     * depends on MethodAnnotationsScanner configured, otherwise an empty set is returned 
     */
    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<Method> result = new HashSet<Method>();

        Set<String> annotatedWith = store.get(MethodAnnotationsScanner.indexName).get(annotation.getName());

        if (annotatedWith != null) {
            for (String annotated : annotatedWith) {
                result.add(getMethodFromString(annotated));
            }
        }
        return result;
    }

    /**
     * get all methods annotated with a given annotation instance
     * including annotation member values matching
     *
     * depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
     */
    public Set<Method> getMethodsAnnotatedWith(Annotation annotation) {
        Set<Method> result = new HashSet<Method>();

        final Class<? extends Annotation> annotationType = annotation.annotationType();
        final Set<Method> annotatedWith = getMethodsAnnotatedWith(annotationType);
        final Map<String, Object> annotationMap = getAnnotationMap(annotation);

        for (Method annotated : annotatedWith) {
            if (annotationMap.equals(getAnnotationMap(annotated.getAnnotation(annotationType)))) {
                result.add(annotated);
            }
        }

        return result;
    }

    /**
     * get all fields annotated with a given annotation class
     *
     * depends on FieldAnnotationsScanner configured, otherwise an empty set is returned
     */
    public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<Field> result = new HashSet<Field>();

        Set<String> annotatedWith = store.get(FieldAnnotationsScanner.indexName).get(annotation.getName());

        if (annotatedWith != null) {
            for (String annotated : annotatedWith) {
                result.add(getFieldFromString(annotated));
            }
        }
        return result;
    }

    /**
     * get all methods annotated with a given annotation instance
     * including annotation member values matching
     *
     * depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
     */
    public Set<Field> getFieldsAnnotatedWith(Annotation annotation) {
        Set<Field> result = new HashSet<Field>();

        final Class<? extends Annotation> annotationType = annotation.annotationType();
        final Set<Field> annotatedWith = getFieldsAnnotatedWith(annotationType);
        final Map<String, Object> annotationMap = getAnnotationMap(annotation);

        for (Field annotated : annotatedWith) {
            if (annotationMap.equals(getAnnotationMap(annotated.getAnnotation(annotationType)))) {
                result.add(annotated);
            }
        }

        return result;
    }

    /**
     * get 'converter' methods that could effectively convert from type 'from' to type 'to'
     *
     * @param from - the one and only parameter indicating the type to convert from
     * @param to   - the required return type
     *             <p/>
     *             depends on ConvertersScanner configured, otherwise an empty set is returned
     */
    public Set<Method> getConverters(final Class<?> from, final Class<?> to) {
        Set<Method> result = new HashSet<Method>();

        String converterKey = ConvertersScanner.getConverterKey(from, to);

        for (String converter : store.get(ConvertersScanner.indexName).get(converterKey)) {
            result.add(getMethodFromString(converter));
        }

        return result;
    }

    //
    protected Set<String> getAllSubTypesInHierarchy(final String type) {
        Set<String> result = new HashSet<String>();

        Set<String> subTypes = store.get(SubTypesScanner.indexName).get(type);
        if (subTypes!=null) {
            result.addAll(subTypes);

            Set<String> subResult = new HashSet<String>();
            for (String aClass : result) {
                subResult.addAll(getAllSubTypesInHierarchy(aClass));
            }
            result.addAll(subResult);
        }

        return result;
    }

    protected Set<String> getAllAnnotatedWithInHierarchy(String annotation) {
        Set<String> result = new HashSet<String>();

        Set<String> annotatedWith = store.get(ClassAnnotationsScanner.indexName).get(annotation);

        if (annotatedWith!=null) {
            result.addAll(annotatedWith);
            for (String aClass : result) {
                result.addAll(getAllAnnotatedWithInHierarchy(aClass));
            }
        }

        return result;
    }

    /**
     * returns a map where keys are annotation's method name and value is the returned value from that method
     */
   protected Map<String/*parameter name*/, Object/*value*/> getAnnotationMap(Annotation annotation) {
        final Method[] methods = annotation.annotationType().getDeclaredMethods();
        Map<String, Object> parameters = new HashMap<String, Object>(methods.length);
        for (final Method method : methods) {
            try {parameters.put(method.getName(), method.invoke(annotation));}
            catch (Exception e) {throw new RuntimeException(e);}
        }

        return parameters;
    }

    protected static Method getMethodFromString(String method) {
        String fullName = method.substring(0, method.indexOf(" "));
        String className = fullName.substring(0,fullName.lastIndexOf("."));
        String methodName = fullName.substring(className.length() + 1);
        String descriptor = method.substring(method.indexOf("(") + 1, method.lastIndexOf(")"));

        List<Class<?>> types = DescriptorHelper.descriptorToTypes(descriptor);
        Class<?>[] p = types.toArray(new Class<?>[types.size()]);

        try {
            return Class.forName(className).getMethod(methodName, p);
        }
        catch (ClassNotFoundException e) {throw new RuntimeException(e);}
        catch (NoSuchMethodException e) {throw new RuntimeException(e);}
    }

    protected static Field getFieldFromString(String field) {
        String className = field.substring(0, field.lastIndexOf('.'));
        String fieldName = field.substring(field.lastIndexOf('.') + 1);

        try {
            return Class.forName(className).getDeclaredField(fieldName);
        }
        catch (ClassNotFoundException e) {throw new RuntimeException(e);}
        catch (NoSuchFieldException e) {throw new RuntimeException(e);}
    }
}
