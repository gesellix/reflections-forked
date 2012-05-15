package org.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.reflections.util.FilterBuilder.prefix;

/**
 * a fluent builder for {@link org.reflections.Configuration}, to be used for constructing a {@link org.reflections.Reflections} instance
 * <p>usage:
 * <pre>
 *      new Reflections(
 *          new ConfigurationBuilder()
 *              .filterInputsBy(new FilterBuilder().include("your project's common package prefix here..."))
 *              .setUrls(ClasspathHelper.forClassLoader())
 *              .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(myClassAnnotationsFilter)));
 * </pre>
 * <br>{@link #executorService} is used optionally used for parallel scanning. if value is null then scanning is done in a simple for loop
 * <p>defaults: accept all for {@link #inputsFilter},
 * {@link #executorService} is null,
 * {@link #serializer} is {@link org.reflections.serializers.XmlSerializer}
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public class ConfigurationBuilder implements Configuration {
    private final Set<Scanner> scanners = Sets.<Scanner>newHashSet(new TypeAnnotationsScanner(), new SubTypesScanner());
    private Set<URL> urls = Sets.newHashSet();
    private MetadataAdapter metadataAdapter = new JavassistAdapter();
    private Predicate<String> inputsFilter = Predicates.alwaysTrue();
    private Serializer serializer;
    private ExecutorService executorService;
    /*@Nullable*/ private ClassLoader[] classLoaders = null;

    public ConfigurationBuilder() {
    }

    /** constructs a {@link ConfigurationBuilder} using the given parameters, in a non statically typed way. that is, each element in {@code params} is
     * guessed by it's type and populated into the configuration.
     * <ul>
     *     <li>{@link String} - would add urls using {@link ClasspathHelper#forPackage(String, ClassLoader...)} ()}</li>
     *     <li>{@link Class} - would add urls using {@link ClasspathHelper#forClass(Class, ClassLoader...)} </li>
     *     <li>{@link ClassLoader} - would use these classloaders in order to find urls in ClasspathHelper.forPackage(), ClasspathHelper.forClass() and for resolving types</li>
     *     <li>{@link Scanner} - would use given scanner, overriding the default scanners</li>
     *     <li>{@link URL} - would add the given url for scanning</li>
     *     <li>{@code Object[]} - would use each element as above</li>
     * </ul>
     *
     * use any parameter type in any order. this constructor uses instanceof on each param and instantiate a {@link ConfigurationBuilder} appropriately.
     * */
    public ConfigurationBuilder(final Object[] params) {
        //flatten
        List<Object> parameters = Lists.newArrayList();
        for (Object param : params) if (!(param instanceof Object[])) parameters.add(param); else for (Object p : (Object[]) param) parameters.add(p);

        List<ClassLoader> loaders = Lists.newArrayList(); for (Object param : parameters) if (param instanceof ClassLoader) loaders.add((ClassLoader) param);

        ClassLoader[] classLoaders = loaders.isEmpty() ? null : loaders.toArray(new ClassLoader[]{});
        FilterBuilder filter = new FilterBuilder();
        List<Scanner> scanners = Lists.newArrayList();

        for (Object param : parameters) {
            if (param instanceof String) { addUrls(ClasspathHelper.forPackage((String) param, classLoaders)); filter.include(prefix((String) param)); }
            else if (param instanceof Class) { addUrls(ClasspathHelper.forClass((Class) param, classLoaders)); filter.includePackage(((Class) param)); }
            else if (param instanceof Scanner) { scanners.add((Scanner) param); }
            else if (param instanceof URL) { addUrls((URL) param); }
            else if (param instanceof ClassLoader) { /* already taken care */ }
            else { if (Reflections.log != null) Reflections.log.warn("could not use param " + param); }
        }

        filterInputsBy(filter);
        if (!scanners.isEmpty()) { setScanners(scanners.toArray(new Scanner[]{})); }
        if (!loaders.isEmpty()) { addClassLoaders(loaders); }
    }

    public Set<Scanner> getScanners() {
		return scanners;
	}

    /** set the scanners instances for scanning different metadata */
    public ConfigurationBuilder setScanners(final Scanner... scanners) {
        this.scanners.clear();
        return addScanners(scanners);
    }

    /** set the scanners instances for scanning different metadata */
    public ConfigurationBuilder addScanners(final Scanner... scanners) {
        this.scanners.addAll(Sets.newHashSet(scanners));
        return this;
    }

    public Set<URL> getUrls() {
        return urls;
    }

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final Collection<URL> urls) {
		this.urls = Sets.newHashSet(urls);
        return this;
	}

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final URL... urls) {
		this.urls = Sets.newHashSet(urls);
        return this;
	}

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final Collection<URL>... urlss) {
        urls.clear();
        addUrls(urlss);
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final Collection<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final URL... urls) {
        this.urls.addAll(Sets.newHashSet(urls));
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final Collection<URL>... urlss) {
        for (Collection<URL> urls : urlss) { addUrls(urls); }
        return this;
    }

    public MetadataAdapter getMetadataAdapter() {
        return metadataAdapter;
    }

    /** sets the metadata adapter used to fetch metadata from classes */
    public ConfigurationBuilder setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
        return this;
    }

    public boolean acceptsInput(String inputFqn) {
        return inputsFilter.apply(inputFqn);
    }

    /** sets the input filter for all resources to be scanned
     * <p> supply a {@link com.google.common.base.Predicate} or use the {@link FilterBuilder}*/
    public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /** sets the executor service used for scanning. */
    public ConfigurationBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as {@link java.lang.Runtime#availableProcessors()}
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor() {
        return useParallelExecutor(Runtime.getRuntime().availableProcessors());
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as the given availableProcessors parameter
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor(final int availableProcessors) {
        setExecutorService(Executors.newFixedThreadPool(availableProcessors));
        return this;
    }

    public Serializer getSerializer() {
        if (serializer == null) {
            serializer = new XmlSerializer(); //lazily defaults to XmlSerializer
        }
        return serializer;
    }

    /** sets the serializer used when issuing {@link org.reflections.Reflections#save} */
    public ConfigurationBuilder setSerializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /** get class loader, might be used for scanning or resolving methods/fields */
    public ClassLoader[] getClassLoaders() {
        return classLoaders;
    }

    /** add class loader, might be used for resolving methods/fields */
    public ConfigurationBuilder addClassLoader(ClassLoader classLoader) {
        return addClassLoaders(classLoader);
    }

    /** add class loader, might be used for resolving methods/fields */
    public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
        this.classLoaders = this.classLoaders == null ? classLoaders : ObjectArrays.concat(this.classLoaders, classLoaders, ClassLoader.class);
        return this;
    }

    /** add class loader, might be used for resolving methods/fields */
    public ConfigurationBuilder addClassLoaders(Collection<ClassLoader> classLoaders) {
        return addClassLoaders(classLoaders.toArray(new ClassLoader[classLoaders.size()]));
    }
}
