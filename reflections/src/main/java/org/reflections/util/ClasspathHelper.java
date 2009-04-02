package org.reflections.util;

import com.google.common.collect.Sets;
import com.google.common.collect.Lists;
import org.reflections.filters.Filter;
import org.reflections.ReflectionsException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.List;

@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
/**
 * Some classpath convenient methods
 */
public abstract class ClasspathHelper {

    /**
     * urls in current classpath from System property java.class.path
     */
    public static Collection<URL> getUrlsForCurrentClasspath() {
        List<URL> urls = Lists.newArrayList();

        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                try {
                    URL url = new File(path).toURI().toURL();
                    urls.add(normalize(url));
                } catch (MalformedURLException e) {
                    throw new ReflectionsException("Can't resolve URLs for current classpath.", e);
                }
            }
        }

        return urls;
    }

    /**
     * actually, urls in current classpath which are directories
     */
    public static Collection<URL> getUrlsForSourcesOnly() {
        List<URL> urls = Lists.newArrayList();

        for (URL url : getUrlsForCurrentClasspath()) {
            File file = new File(url.getFile());
            if (file.isDirectory()) {
                urls.add(url);
            }
        }
        return urls;
    }

    /**
     * the url that contains the given class.
     */
    public static URL getUrlForClass(Class<?> aClass) {
        String className = aClass.getName();

        URL result;
        try {
            final ClassLoader loader = Utils.getEffectiveClassLoader();
            URL classUrl = loader.getResource(DescriptorHelper.classNameToResourceName(className));
            final String resourceName = DescriptorHelper.classNameToResourceName(className);

            final String classUrlString = classUrl.toString();
            String baseUrlName = classUrlString.substring(0, classUrlString.indexOf(resourceName));

            result = normalize(new URL(baseUrlName));
        } catch (Exception e) {
            throw new ReflectionsException("Can't resolve URL for class " + className, e);
        }
        return result;
    }

    /**
     * returns a set of urls that contain resources with prefix as the given parameter, that is exist in
     * the equivalent directory within the urls of current classpath
     */
    public static Collection<URL> getUrlsForPackagePrefix(String packagePrefix) {
        final List<URL> urls = Lists.newArrayList();
        String packageResourcePrefix = DescriptorHelper.qNameToResourceName(packagePrefix);
        try {
            final Enumeration<URL> urlEnumeration = Utils.getEffectiveClassLoader().getResources(packageResourcePrefix);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                urls.add(normalize(url));
            }

            return urls;
        } catch (IOException e) {
            throw new ReflectionsException("Can't resolve URL for package prefix " + packagePrefix, e);
        }
    }

    /**
     * searches for resources accepted by the given resourceNameFilter within the given urls.
     * each url is assumed to contain only one such file at most.
     *
     * the urls provided might be the full classpath (getUrlsForCurrentClasspath), but it is better
     * to provide a narrowed list of urls using getUrlsForPackagePrefix
     */
    public static Set<String> getMatchingJarResources(final Collection<URL> urls, final Filter<String> resourceNameFilter) {
        final Set<String> matchingJarResources = Sets.newHashSet();

        for (URL url : urls) {
            for (VirtualFile virtualFile : VirtualFile.iterable(url)) {
                String resourceName = virtualFile.getName();

                if (resourceNameFilter.accept(resourceName)) {
                    matchingJarResources.add(resourceName);
                    break; //only one
                }
            }
        }

        return matchingJarResources;
    }

    //todo: this is only partial, probably
    public static URL normalize(URL url) throws MalformedURLException {
        String spec = url.getFile();

        //get url base - remove everything after ".jar!/??" , if exists
        final int i = spec.indexOf("!/");
        if (i != -1) {
            spec = spec.substring(0, spec.indexOf("!/"));
        }

        //uppercase windows drive
        url = new URL(url, spec);
        final String file = url.getFile();
        final int i1 = file.indexOf(':');
        if (i1 != -1) {
            String drive = file.substring(i1 - 1, 2).toUpperCase();
            url = new URL(url, file.substring(0, i1 - 1) + drive + file.substring(i1));
        }

        return url;
    }

}

