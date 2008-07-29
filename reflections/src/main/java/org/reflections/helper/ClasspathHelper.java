package org.reflections.helper;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mamo
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class ClasspathHelper {

    public static Set<URL> getUrlsForCurrentClasspath() {
        Set<URL> urls = Sets.newHashSet();

        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                try {
                    URL url = new File(path).toURI().toURL();
                    urls.add(normalizeAndLocalizeUrl(url));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e); //todo: better log
                }
            }
        }

        return urls;
    }

    public static Set<URL> getUrlsForSourcesOnly() {
        Set<URL> urls = Sets.newHashSet();

        for (URL url : getUrlsForCurrentClasspath()) {
            File file = new File(url.getFile());
            if (file.isDirectory()) {
                urls.add(url);
            }
        }
        return urls;
    }

    public static URL getUrlForClassName(String className) {
        try {
            final ClassLoader loader = getClassLoader();
            URL classUrl = loader.getResource(classNameToResourceName(className));
            String baseUrlName = classUrl.toString().split(className)[0];

            return normalizeAndLocalizeUrl(new URL(baseUrlName));
        } catch (Exception e) {
            throw new RuntimeException(e); //todo: better log
        }
    }

    public static URL getUrlForClass(Class<?> aClass) {
        return getUrlForClassName(aClass.getName());
    }

    public static Set<String> getMatchingJarResources(Set<URL> urls, final String packagePrefix, Pattern pattern) {
        final Set<String> matchingJarResources = Sets.newHashSet();

        String prefix = packagePrefix != null ? packagePrefix : "";

        //find relevant urls:
        Set<URL> relevantUrls;
        //try to narrow all urls by using getUrlsForPackagePrefix
        if (prefix.length() != 0) {
            relevantUrls= getUrlsForPackagePrefix(prefix);
            //todo mamo >> intersect with given urls, notice that formats of url string differs
//            relevantUrls = Sets.intersection(urls, forPackagePrefix);

        } else {
            relevantUrls = urls;
        }

        for (URL relevantUrl : relevantUrls) {
            //iterate over url's entries
            final Iterator<String> namesIterator = UrlIterators.createNamesIterator(relevantUrl);

            while (namesIterator.hasNext()) {
                String resourceName = namesIterator.next();

                if (resourceName.startsWith(prefix)) {
                    if (pattern.matcher(resourceName.substring(prefix.length())).matches()) { //if the rest matches the pattern
                        matchingJarResources.add(resourceName);
                    }
                }
            }
        }

        return matchingJarResources;
    }

    public static Set<URL> getUrlsForPackagePrefix(String packagePrefix) {
        final Set<URL> urls = Sets.newHashSet();
        String packageResourcePrefix = qNameToResourceName(packagePrefix);
        try {
            final Enumeration<URL> urlEnumeration = getClassLoader().getResources(packageResourcePrefix);
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                urls.add(normalizeAndLocalizeUrl(url));
            }

            return urls;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    public static String classToResourceName(Class<?> aClass) {
        return classNameToResourceName(aClass.getName());
    }

    public static String classNameToResourceName(final String className) {
        return qNameToResourceName(className) + ".class";
    }

    public static String qNameToResourceName(String qName) {
        return qName.replace(".", "/");
    }

    //todo: this is only partial
    @SuppressWarnings({"AssignmentToMethodParameter"})
    public static URL normalizeAndLocalizeUrl(URL url) throws MalformedURLException {
        String spec = url.getFile();

        //get url base - remove everything after ".jar!/??" , if exists
        final int i = spec.indexOf("!/");
        if (i != -1) {
            spec = spec.substring(0, spec.indexOf("!/"));
        }

        //lowercase windows drive
        url = new URL(url, spec);
        final String file = url.getFile();
        final int i1 = file.indexOf(":");
        if (i1 != -1) {
            String drive = file.substring(i1 - 1, 2).toUpperCase();
            url = new URL(url, file.substring(0, i1 - 1) + drive + file.substring(i1));
        }

        return url;
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

