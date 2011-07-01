package org.reflections.util;

import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.vfs.Vfs;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Some classpath convenient methods
 */
public abstract class ClasspathHelper {

    public static ClassLoader[] defaultClassLoaders = new ClassLoader[]{getContextClassLoader(), getStaticClassLoader()};

    /** returns {@code Thread.currentThread().getContextClassLoader()} */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /** returns {@code Reflections.class.getClassLoader()} */
    public static ClassLoader getStaticClassLoader() {
        return Reflections.class.getClassLoader();
    }

    /** returns given classLoaders, if not null,
     * otherwise defaults to both {@link #getContextClassLoader()} and {@link #getStaticClassLoader()} */
    public static ClassLoader[] classLoaders(ClassLoader... classLoaders) {
        return classLoaders != null && classLoaders.length != 0 ? classLoaders : defaultClassLoaders;
    }

    /** returns urls with resources of package starting with given name, using {@link ClassLoader#getResources(String)}
     * <p>that is, forPackage("org.reflections") effectively returns urls from classpath with packages starting with {@code org.reflections}
     * <p>if optional {@link ClassLoader}s are not specified, then both {@link #getContextClassLoader()} and {@link #getStaticClassLoader()} are used for {@link ClassLoader#getResources(String)}
     */
    public static Set<URL> forPackage(String name, ClassLoader... classLoaders) {
        final Set<URL> result = Sets.newHashSet();

        final ClassLoader[] loaders = classLoaders(classLoaders);
        final String resourceName = name.replace(".", "/");

        for (ClassLoader classLoader : loaders) {
            try {
                final Enumeration<URL> urls = classLoader.getResources(resourceName);
                while (urls.hasMoreElements()) {
                    final URL url = urls.nextElement();
                    final URL normalizedUrl = new URL(url.toExternalForm().substring(0, url.toExternalForm().lastIndexOf(resourceName)));
                    result.add(normalizedUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /** returns the url that contains the given class, using {@link ClassLoader#getResource(String)}
     * <p>if optional {@link ClassLoader}s are not specified, then either {@link #getContextClassLoader()} or {@link #getStaticClassLoader()} are used for {@link ClassLoader#getResources(String)}
     * */
    public static URL forClass(Class<?> aClass, ClassLoader... classLoaders) {
        final ClassLoader[] loaders = classLoaders(classLoaders);
        final String resourceName = aClass.getName().replace(".", "/") + ".class";

        for (ClassLoader classLoader : loaders) {
            try {
                final URL url = classLoader.getResource(resourceName);
                if (url != null) {
                    final String normalizedUrl = url.toExternalForm().substring(0, url.toExternalForm().lastIndexOf(aClass.getPackage().getName().replace(".", "/")));
                    return new URL(normalizedUrl);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    
    /** returns urls using {@link java.net.URLClassLoader#getURLs()} up the classloader parent hierarchy
     * <p>if optional {@link ClassLoader}s are not specified, then both {@link #getContextClassLoader()} and {@link #getStaticClassLoader()} are used for {@link ClassLoader#getResources(String)}
     * */
    public static Set<URL> forClassLoader(ClassLoader... classLoaders) {
        final Set<URL> result = Sets.newHashSet();

        final ClassLoader[] loaders = classLoaders(classLoaders);

        for (ClassLoader classLoader : loaders) {
            while (classLoader != null) {
                if (classLoader instanceof URLClassLoader) {
                    URL[] urls = ((URLClassLoader) classLoader).getURLs();
                    if (urls != null) {
                        result.addAll(Sets.<URL>newHashSet(urls));
                    }
                }
                classLoader = classLoader.getParent();
            }
        }

        return result;
    }

    /** returns urls using {@code java.class.path} system property */
    public static Set<URL> forJavaClassPath() {
        Set<URL> urls = Sets.newHashSet();

        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                try { urls.add(new File(path).toURI().toURL()); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }

        return urls;
    }

    /** returns urls using {@link ServletContext} in resource path WEB-INF/lib */
    public static Set<URL> forWebInfLib(final ServletContext servletContext) {
        final Set<URL> urls = Sets.newHashSet();

        for (Object urlString : servletContext.getResourcePaths("/WEB-INF/lib")) {
            try { urls.add(servletContext.getResource((String) urlString)); }
            catch (MalformedURLException e) { /*fuck off*/ }
        }

        return urls;
    }

    /** returns url using {@link ServletContext} in resource path WEB-INF/classes */
    public static URL forWebInfClasses(final ServletContext servletContext) {
        try {
            final String path = servletContext.getRealPath("/WEB-INF/classes");
            final File file = new File(path);
            if (file.exists()) return file.toURL();
        }
        catch (MalformedURLException e) { /*fuck off*/ }

        return null;
    }

    /** return urls that are in the current class path.
     * attempts to load the jar manifest, if any, and adds to the result any dependencies it finds. */
    public static Set<URL> forManifest() {
        return forManifest(forClassLoader());
    }

    /** get the urls that are specified in the manifest of the given url for a jar file.
     * attempts to load the jar manifest, if any, and adds to the result any dependencies it finds. */
    public static Set<URL> forManifest(final URL url) {
        final Set<URL> result = Sets.newHashSet();

        result.add(url);

        try {
            final String part = Vfs.normalizePath(url);
            File jarFile = new File(part);
            JarFile myJar = new JarFile(part);

            URL validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), part);
            if (validUrl != null) { result.add(validUrl); }

            final Manifest manifest = myJar.getManifest();
            if (manifest != null) {
                final String classPath = manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
                if (classPath != null) {
                    for (String jar : classPath.split(" ")) {
                        validUrl = tryToGetValidUrl(jarFile.getPath(), new File(part).getParent(), jar);
                        if (validUrl != null) { result.add(validUrl); }
                    }
                }
            }
        } catch (IOException e) {
            // don't do anything, we're going on the assumption it is a jar, which could be wrong
        }

        return result;
    }

    /** get the urls that are specified in the manifest of the given urls.
     * attempts to load the jar manifest, if any, and adds to the result any dependencies it finds. */
    public static Set<URL> forManifest(final Iterable<URL> urls) {
        Set<URL> result = Sets.newHashSet();

        // determine if any of the URLs are JARs, and get any dependencies
        for (URL url : urls) {
            result.addAll(forManifest(url));
        }

        return result;
    }

    //a little bit cryptic...
    private static URL tryToGetValidUrl(String workingDir, String path, String filename) {
        try {
            if (new File(filename).exists())
                return new File(filename).toURI().toURL();
            if (new File(path + File.separator + filename).exists())
                return new File(path + File.separator + filename).toURI().toURL();
            if (new File(workingDir + File.separator + filename).exists())
                return new File(workingDir + File.separator + filename).toURI().toURL();
        } catch (MalformedURLException e) {
            // don't do anything, we're going on the assumption it is a jar, which could be wrong
        }
        return null;
    }
}

