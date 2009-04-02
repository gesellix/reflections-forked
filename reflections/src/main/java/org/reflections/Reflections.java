package org.reflections;

import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.newArray;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.XStream;
import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ParallelArray;
import jsr166y.forkjoin.ForkJoinPool;
import org.reflections.filters.Filter;
import org.reflections.scanners.*;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.DescriptorHelper;
import org.reflections.util.Utils;
import static org.reflections.util.ReflectionUtil.resolveClass;
import static org.reflections.util.Utils.forNames;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 Reflections one-stop-shop object.
 use it to either
 scan metadata - according to a given Configuration
 or
 collect saved xml resources - according to urls and a filter
 <p/>
 than you can use it for various metadata queries
 */
public class Reflections {
	private static final Logger log = LoggerFactory.getLogger(Reflections.class);

	private static final Pattern METHOD_PATTERN = Pattern.compile("(.*)\\.(.*) (.*)");

	private Configuration configuration;
	private final Store store;
	private final ForkJoinPool forkJoinPool = new ForkJoinPool();

	/** Reflections scan according to configuration */
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

	/** Reflections collect saved xml resources */
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
		Split split = SimonManager.getStopwatch("FullScan").start();
		Iterable<Object> classesIterator = configuration.getMetadataAdapter().iterateClasses(configuration.getUrls());
		for (final Object cls : classesIterator) {
			for (Scanner scanner : configuration.getScanners()) {
				scanner.scan(cls);
			}
		}
		split.stop();
		log.info("Scanning classes took " + split.stop() / 1000000 + " ms");
	}

	/** merges a Reflections instance into this one */
	public void merge(final Reflections reflections) {
		store.merge(reflections.store);
	}

	/** saves the store into a given destination as xml file */
	public void save(final String destination) {
		try {
			String xml = new XStream().toXML(store);
			Utils.safeWriteFile(xml, new File(destination));
		} catch (IOException e) {
			throw new ReflectionsException(String.format("could not save file %s", destination), e);
		}
	}

	//query

	/**
	 gets all sub types in hierarchy of a given type
	 <p/>
	 depends on SubTypesScanner configured, otherwise an empty set is returned
	 */
	public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
		//noinspection RedundantTypeArguments
		return Utils.<T>forNames(getAllSubTypesInHierarchy(type.getName()));
	}

	/**
	 get all types annotated with a given annotation class in hierarchy
	 <p/>
	 depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
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
	 get all types annotated with a given annotation instance in hierarchy
	 including annotation member values matching
	 <p/>
	 depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
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
	 Get all methods annotated with a given annotation class
	 <p/>
	 Depends on MethodAnnotationsScanner configured, otherwise an empty set is returned
	 */
	public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
		Collection<String> annotatedWith = store.get(MethodAnnotationsScanner.indexName).get(annotation.getName());
		if (annotatedWith != null) {
			ParallelArray<String> parallelArray = ParallelArray.createFromCopy(newArray(annotatedWith, String.class), forkJoinPool);
			return ImmutableSet.of(parallelArray.withMapping(new Ops.Mapper<String, Method>() {
				public Method map(String annotated) {
					return getMethodFromString(annotated);
				}
			}).all().getArray());
//			return ImmutableSet.of(methods);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 get all methods annotated with a given annotation instance
	 including annotation member values matching
	 <p/>
	 depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
	 */
	public Set<Method> getMethodsAnnotatedWith(Annotation annotation) {
		Set<Method> result = Sets.newHashSet();

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
	 get all fields annotated with a given annotation class
	 <p/>
	 depends on FieldAnnotationsScanner configured, otherwise an empty set is returned
	 */
	public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
		Collection<String> annotatedWith = store.get(FieldAnnotationsScanner.indexName).get(annotation.getName());
		if (annotatedWith != null) {
			ParallelArray<String> parallelArray = ParallelArray.createFromCopy(newArray(annotatedWith, String.class), forkJoinPool);
			return ImmutableSet.of(parallelArray.withMapping(new Ops.Mapper<String, Field>() {
				public Field map(String annotated) {
					return getFieldFromString(annotated);
				}
			}).all().getArray());
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 get all methods annotated with a given annotation instance
	 including annotation member values matching
	 <p/>
	 depends on ClassAnnotationsScanner configured, otherwise an empty set is returned
	 */
	public Set<Field> getFieldsAnnotatedWith(Annotation annotation) {
		Set<Field> result = Sets.newHashSet();

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
	 get 'converter' methods that could effectively convert from type 'from' to type 'to'

	 @param from - the one and only parameter indicating the type to convert from
	 @param to   - the required return type
	 <p/>
	 depends on ConvertersScanner configured, otherwise an empty set is returned
	 */
	public Set<Method> getConverters(final Class<?> from, final Class<?> to) {
		Set<Method> result = Sets.newHashSet();

		String converterKey = ConvertersScanner.getConverterKey(from, to);

		for (String converter : store.get(ConvertersScanner.indexName).get(converterKey)) {
			result.add(getMethodFromString(converter));
		}

		return result;
	}

	//
	protected Set<String> getAllSubTypesInHierarchy(final String type) {
		Set<String> result = Sets.newHashSet();

		Collection<String> subTypes = store.get(SubTypesScanner.indexName).get(type);
		if (subTypes != null) {
			result.addAll(subTypes);

			Set<String> subResult = Sets.newHashSet();
			for (String aClass : result) {
				subResult.addAll(getAllSubTypesInHierarchy(aClass));
			}
			result.addAll(subResult);
		}

		return result;
	}

	protected Set<String> getAllAnnotatedWithInHierarchy(String annotation) {
		Set<String> result = Sets.newHashSet();

		Collection<String> annotatedWith = store.get(ClassAnnotationsScanner.indexName).get(annotation);

		if (annotatedWith != null) {
			result.addAll(annotatedWith);
			for (String aClass : result) {
				result.addAll(getAllAnnotatedWithInHierarchy(aClass));
			}
		}

		return result;
	}

	/** returns a map where keys are annotation's method name and value is the returned value from that method */
	protected Map<String/*parameter name*/, Object/*value*/> getAnnotationMap(Annotation annotation) {
		final Method[] methods = annotation.annotationType().getDeclaredMethods();
		Map<String, Object> parameters = new HashMap<String, Object>(methods.length);
		for (final Method method : methods) {
			try {
				parameters.put(method.getName(), method.invoke(annotation));
			} catch (Exception e) {
				throw new ReflectionsException("Error while invoking method " + method, e);
			}
		}

		return parameters;
	}

	protected static Method getMethodFromString(String method) throws ReflectionsException {
		Matcher matcher = METHOD_PATTERN.matcher(method);
		if (!matcher.matches()) {
			throw new ReflectionsException("method " + method + " doesn't fit the method regex pattern. This is probably a problem with the pattern.");
		}
		matcher.group(1);
		String className = matcher.group(1);
		String methodName = matcher.group(2);
		String parametersDescriptor = matcher.group(3);
		List<Class<?>> types = DescriptorHelper.descriptorToTypes(parametersDescriptor);
		Class<?>[] p = types.toArray(new Class<?>[types.size()]);

		try {
			return resolveClass(className).getMethod(methodName, p);
		} catch (NoSuchMethodException e) {
			throw new ReflectionsException("Can't resolve method named " + methodName, e);
		}
	}

	protected static Field getFieldFromString(String field) {
		String className = field.substring(0, field.lastIndexOf('.'));
		String fieldName = field.substring(field.lastIndexOf('.') + 1);

		try {
			return resolveClass(className).getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ReflectionsException("Can't resolve field named " + fieldName, e);
		}
	}
}
