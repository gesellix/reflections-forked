package org.reflections.model;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.reflections.helper.ReflectionsConstants;
import static org.reflections.model.ElementTypes.supertypes;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * contains all raw configuration needed by reflections.
 * To use it, use Configuraion.build(...) passing some (optionaly) convenient ConfigurationBuilder
 * and use the setters here to (fine) tune it
 *
 * for example, to create a configuration that results in scanning annotations and computing transitive closure on current class's url:
 *      Configuration.build(Annotations, Transitive, ThisUrl);
 * which is the same as:
 *      Configuration.build()
 *          .addElementTypesToScan(ElementTypes.annotations)
 *          .setComputeTransitiveClosure(true)
 *          .addUrls(ClasspathHelper.getUrlForClass(this.getClass()));
 *
 * @author mamo
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
public abstract class Configuration {

    private Set<ElementTypes> elementTypesToScan = Sets.newEnumSet(Iterables.<ElementTypes>emptyIterable(), ElementTypes.class);
    private boolean computeTransitiveClosure;
    private Set<ElementTypes> reverseElementTypes = Sets.newEnumSet(Iterables.<ElementTypes>emptyIterable(), ElementTypes.class);
    private Set<URL> urls = Sets.newHashSet();
    private Set<Pattern> excludePatterns = Sets.newHashSet();
    private Set<Pattern> includePatterns = Sets.newHashSet();
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
        dependsOnElementsTypesToScan(supertypes);
        this.computeTransitiveClosure = computeTransitiveClosure;
        return this;
    }

    public Configuration addReverseElementTypes(ElementTypes... invertedElementTypes) {
        this.reverseElementTypes.addAll(Arrays.asList(invertedElementTypes));
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

    public Configuration addExcludePatterns(Pattern... patterns) {
        this.excludePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    public Configuration addIncludePatterns(Pattern... fqnPrefixes) {
        this.includePatterns.addAll(Arrays.asList(fqnPrefixes));
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

    public boolean shouldComputeReverseIndices() {return !reverseElementTypes.isEmpty();}

    public Set<ElementTypes> getReverseElementTypes() {
        return reverseElementTypes;
    }

    public Set<URL> getUrls() {return urls;}

    public Set<Pattern> getExcludePatterns() {return excludePatterns;}

    public Set<Pattern> getIncludePatterns() {return includePatterns;}

    public String getPostCompiledResourcesPackagePrefix() {return postCompiledResourcesPackagePrefix;}

    public String getPostCompiledResourcesPattern() {return postCompiledResourcesPattern;}

    public boolean shouldFetchPreCompiledResources() {return fetchPostCompiledResources;}
}
