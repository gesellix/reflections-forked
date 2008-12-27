package org.reflections.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Iterators;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * a File and JarFile bridge
 */
public abstract class VirtualFile {
    public abstract InputStream getInputStream();

    public abstract String getName();

    //
    public static VirtualFile create(final File file) {
        return new VirtualFile() {
            public InputStream getInputStream() {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            public String getName() {
                return file.getName();
            }
        };
    }

    public static VirtualFile create(final JarFile jarFile, final JarEntry entry) {
        return new VirtualFile() {
            public InputStream getInputStream() {
                try {
                    return jarFile.getInputStream(entry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String getName() {
                return entry.getName();
            }
        };
    }

    //
    public static Iterable<VirtualFile> iterable(final Collection<URL> urls) {
        return FluentIterable.iterate(urls).fork(urls2VirtualFiles);
    }

    public static Iterable<VirtualFile> iterable(final URL url) {
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (isDirectory(url)) {
            return iterable(new File(uri));
        }

        if (isJar(url)) {
            try {
                return iterable(new JarFile(new File(uri)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("could not create iterator of VirtualFiles from url " + url);
    }

    public static Iterable<VirtualFile> iterable(final File dir) {
        return new Iterable<VirtualFile>() {
            public Iterator<VirtualFile> iterator() {
                return new DirFilesIterator(dir);
            }
        };
    }

    public static Iterable<VirtualFile> iterable(final JarFile jarFile) {
        return new Iterable<VirtualFile>() {
            public Iterator<VirtualFile> iterator() {
                return new JarFileIterator(jarFile);
            }
        };
    }

    //
    public static boolean isJar(final URL url) {
        return url.getFile().endsWith(".jar");
    }

    public static boolean isDirectory(final URL url) {
        return url.getFile().endsWith("/");
    }

    //

    /**
     * iterates files recursively over a given root directory
     */
    public static class DirFilesIterator extends AbstractIterator<VirtualFile> {
        private final Stack<File> fileStack;

        public DirFilesIterator(final File dir) {
            if (!dir.isDirectory()) {throw new RuntimeException(dir + "is not a directory");}
            fileStack = new Stack<File>();
            fileStack.add(dir);
        }

        private void addFilesFromDir(final File dir) {
            fileStack.addAll(Lists.newArrayList(dir.listFiles()));
        }

        @Override
        protected VirtualFile computeNext() {
            while (!fileStack.isEmpty()) {
                File file = fileStack.pop();

                if (file.isDirectory()) {
                    addFilesFromDir(file);
                } else {
                    return create(file);
                }
            }

            return endOfData();
        }
    }

    /**
     * iterates jar entries recursively(?) over a given jar file
     */
    public static class JarFileIterator extends AbstractIterator<VirtualFile> {
        private final Iterator<JarEntry> entries;
        private final JarFile jarFile;

        public JarFileIterator(final JarFile jarFile) {
            this.jarFile = jarFile;
            entries = Iterators.forEnumeration(jarFile.entries());
        }

        protected VirtualFile computeNext() {
            if (entries.hasNext()) {
                return create(jarFile, entries.next());
            } else {
                return endOfData();
            }
        }
    }

    public static final Transformer<URL, Iterator<VirtualFile>> urls2VirtualFiles =
            new Transformer<URL, Iterator<VirtualFile>>() {
                public Iterator<VirtualFile> transform(final URL url) {
                    return VirtualFile.iterable(url).iterator();
                }
            };

}
