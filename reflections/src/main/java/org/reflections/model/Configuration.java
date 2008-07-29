package org.reflections.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.Arrays;
import java.net.URL;

import static org.reflections.model.ElementTypes.*;
import org.reflections.helper.ReflectionsConstants;

/**
 * @author mamo
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
public abstract class Configuration {

    private Set<ElementTypes> elementTypesToScan = Sets.newEnumSet(Iterables.<ElementTypes>emptyIterable(), ElementTypes.class);
    private boolean computeTransitiveClosure;
    private Set<ElementTypes> invertedElementTypes = Sets.newEnumSet(Iterables.<ElementTypes>emptyIterable(), ElementTypes.class);
    private Set<URL> urls = Sets.newHashSet();
    private Set<String> excludedFqnPrefixes = Sets.newHashSet();
    private Set<String> includeFqnPrefixes = Sets.newHashSet();
    private boolean scanSources;
    private boolean fetchPostCompiledResources;
    private String postCompiledResourcesPattern = ReflectionsConstants.DEFAULT_POST_COMPILED_RESOURCES_PATTERN;
    private String postCompiledResourcesPackagePrefix = ReflectionsConstants.DEFAULT_POST_COMPILED_RESOURCES_PACKAGE_PREFIX;

    //
    Configuration() {}

    public static Configuration build(ConfigurationBuilder... builders) {return ConfigurationBuilder.build(builders);}

    //
    public Configuration addElementTypesToScan(ElementTypes... elementTypes) {
        this.elementTypesToScan.addAll(Arrays.asList(elementTypes));
        return this;
    }

    public Configuration setComputeTransitiveClosure(boolean computeTransitiveClosure) {
        dependsOnElementsTypesToScan(supertypes, interfaces);
        this.computeTransitiveClosure = computeTransitiveClosure;
        return this;
    }

    public Configuration addInvertedElementTypes(ElementTypes... invertedElementTypes) {
        this.invertedElementTypes.addAll(Arrays.asList(invertedElementTypes));
        return this;
    }

    public Configuration addUrls(Set<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }

    public Configuration addUrls(URL... urls) {
        this.urls.addAll(Arrays.asList(urls));
        return this;
    }

    public Configuration addExcludedFqnsPrefixes(String... fqnsPrefixes) {
        this.excludedFqnPrefixes.addAll(Arrays.asList(fqnsPrefixes));
        return this;
    }

    public Configuration addIncludedFqnPrefixes(String... fqnPrefixes) {
        this.includeFqnPrefixes.addAll(Arrays.asList(fqnPrefixes));
        return this;
    }

    public Configuration setScanSources(boolean scanSources) {
        this.scanSources = scanSources;
        return this;
    }

    public Configuration setFetchPostCompiledResources(boolean fetchPostCompiledResources) {
        this.fetchPostCompiledResources = fetchPostCompiledResources;
        return this;
    }

    public Configuration setPostCompiledResourcesPattern(String postCompiledResourcesPackagePrefix, String postCompiledResourcesPattern) {
        setFetchPostCompiledResources(true); //depends
        this.postCompiledResourcesPackagePrefix = postCompiledResourcesPackagePrefix;
        this.postCompiledResourcesPattern = postCompiledResourcesPattern;
        return this;
    }

    private void dependsOnElementsTypesToScan(ElementTypes... elementTypes) {addElementTypesToScan(elementTypes);}

    //
    public Set<ElementTypes> getElementTypesToScan() {
        return elementTypesToScan;
    }

    public boolean shouldComputeTransitiveClosure() {return computeTransitiveClosure;}

    public boolean shouldComputeInvertedIndexes() {return !invertedElementTypes.isEmpty();}

    public Set<ElementTypes> getInvertedElementTypes() {
        return invertedElementTypes;
    }

    public Set<URL> getUrls() {return urls;}

    public Set<String> getExcludedFqnPrefixes() {
        return excludedFqnPrefixes;
    }

    public Set<String> getIncludeFqnPrefixes() {
        return includeFqnPrefixes;
    }

    public String getPostCompiledResourcesPackagePrefix() {return postCompiledResourcesPackagePrefix;}

    public String getPostCompiledResourcesPattern() {return postCompiledResourcesPattern;}

    public boolean shouldScanSources() {return scanSources;}

    public boolean shouldFetchPreCompiledResources() {return fetchPostCompiledResources;}
}
