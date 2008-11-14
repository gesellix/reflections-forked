package org.reflections;

import com.thoughtworks.xstream.XStream;
import org.reflections.filters.Any;
import org.reflections.filters.Filter;
import org.reflections.scanners.*;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.DescriptorHelper;
import org.reflections.util.Utils;
import static org.reflections.util.Utils.forNames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
        //inject
        for (Scanner scanner : configuration.getScanners()) {
            scanner.setConfiguration(configuration);
            scanner.setStore(store.get(scanner.getIndexName()));
        }

        //scan classes
        Iterator<Object> classesIterator = configuration.getMetadataAdapter().iterateClasses(configuration.getUrls(), new Any<String>());

        while (classesIterator.hasNext()) {
            Object cls = classesIterator.next();

            for (Scanner scanner : configuration.getScanners()) {
                scanner.scan(cls);
            }
        }
    }

    public void merge(final Reflections reflections) {
        store.merge(reflections.store);
    }

    public void save(final String destination) {
        try {
            String xml = new XStream().toXML(store);
            Utils.safeWriteFile(xml, new File(destination));
        } catch (IOException e) {
            throw new RuntimeException(String.format("could not save file %s", destination), e);
        }
    }

    //query
    public <T> Set<Class<?>> getSubTypesOf(Class<T> type) {
        return forNames(getSubTypesClosure(type.getName()));
    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        Set<String> result = new HashSet<String>();

        for (String aClass : getAnnotatedWithClosure(annotation.getName())) {
            result.add(aClass);
            Set<String> subTypes = getSubTypesClosure(aClass);
            if (!subTypes.isEmpty()) {
                result.addAll(subTypes);
            }
        }

        return forNames(result);
    }

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

    public Set<Method> getConverters(final Class<?> from, final Class<?> to) {
        Set<Method> result = new HashSet<Method>();

        Set<String> converters = store.get(ConvertersScanner.indexName).get(ConvertersScanner.getConverterKey(from, to));

        for (String converter : converters) {
            result.add(getMethodFromString(converter));
        }

        return result;
    }

    //
    protected Set<String> getAnnotatedWithClosure(String annotation) {
        Set<String> result = new HashSet<String>();

        Set<String> annotatedWith = store.get(ClassAnnotationsScanner.indexName).get(annotation);

        if (annotatedWith!=null) {
            result.addAll(annotatedWith);
            for (String aClass : result) {
                result.addAll(getAnnotatedWithClosure(aClass));
            }
        }

        return result;
    }

    protected Set<String> getSubTypesClosure(final String type) {
        Set<String> result = new HashSet<String>();

        Set<String> subTypes = store.get(SubTypesScanner.indexName).get(type);
        if (subTypes!=null) {
            result.addAll(subTypes);
            for (String aClass : result) {
                result.addAll(getSubTypesClosure(aClass));
            }
        }

        return result;
    }

    protected Method getMethodFromString(String method) {
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
}
