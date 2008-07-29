package org.reflections.helper;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author mamo
 */

@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class UrlIterators {

    //
    public static Iterator<DataInputStream> createStreamIterator(final URL url) {
        try {
            if (isDirectory(url)) {return new DirStreamIterator(new File(url.toURI()));}
            if (isJar(url)) {return new JarStreamIterator(new JarFile(url.getFile()));}
        } catch (Exception e) {
            throw new RuntimeException(e); //todo: better log
        }

        return null;
    }

    public static Iterator<String> createNamesIterator(final URL url) {
        try {
            if (isDirectory(url)) {return new DirNamesIterator(new File(url.toURI()));}
            if (isJar(url)) {return new JarNamesIterator(new JarFile(url.toURI().getPath()));}
        } catch (Exception e) {
            throw new RuntimeException(e); //todo: better log
        }

        return null;
    }

    private static boolean isJar(URL url) {return url.getFile().endsWith(".jar");}

    private static boolean isDirectory(URL url) {return url.getFile().endsWith("/");}

    //
    public static class DirFilesIterator extends AbstractIterator<File> {
        private final Stack<File> fileStack = new Stack<File>();

        public DirFilesIterator(File rootDir) {
            if (!rootDir.isDirectory()) {
                throw new RuntimeException(rootDir + " should be a directory");
            }

            addFilesFromDir(rootDir);
        }

        private void addFilesFromDir(final File dir) {
            fileStack.addAll(Lists.newArrayList(dir.listFiles()));
        }

        @Override
        protected File computeNext() {
            while (!fileStack.isEmpty()) {
                File file = fileStack.pop();

                if (file.isDirectory()) {
                    addFilesFromDir(file);
                } else {
                    if (accepts(file)) {return file;}
                }
            }

            return endOfData();
        }

        protected boolean accepts(File file) {return true;}
    }

    @SuppressWarnings({"PackageVisibleField"})
    public static class JarEntryIterator extends AbstractIterator<JarEntry> {
        final JarFile jarFile;
        final Iterator<JarEntry> entries;

        public JarEntryIterator(final JarFile jarFile) {
            this.jarFile = jarFile;
            entries = Iterators.forEnumeration(jarFile.entries());
        }

        @Override
        protected JarEntry computeNext() {

            while (entries.hasNext()) {
                JarEntry jarEntry = entries.next();
                if (accepts(jarEntry)) {return jarEntry;}
            }

            return endOfData();
        }

        protected boolean accepts(JarEntry jarEntry) {return true;}
    }

    //
    public static class DirNamesIterator extends AbstractIterator<String> {
        private final DirFilesIterator dirFilesIterator;

        public DirNamesIterator(final File rootDir) {dirFilesIterator = new DirFilesIterator(rootDir);}

        @Override
        protected String computeNext() {
            return dirFilesIterator.hasNext() ? dirFilesIterator.next().getName() : endOfData();
        }
    }

    public static class DirClassIterator extends DirFilesIterator {
        public DirClassIterator(File rootDir) {super(rootDir);}

        @Override
        protected boolean accepts(File file) {return file.getName().endsWith(".class");}
    }

    public static class DirStreamIterator extends AbstractIterator<DataInputStream> {
        @SuppressWarnings({"PackageVisibleField"})
        final DirClassIterator dirClassIterator;

        public DirStreamIterator(File file) {
            dirClassIterator = new DirClassIterator(file);
        }

        @Override
        protected DataInputStream computeNext() {
            try {
                if (dirClassIterator.hasNext()) {
                    final File aFile = dirClassIterator.next();
                    return new DataInputStream(new BufferedInputStream(
                            new FileInputStream(aFile)));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e); //todo: better log
            }
            return endOfData();
        }
    }

    //
    public static class JarClassIterator extends JarEntryIterator {
        public JarClassIterator(JarFile jarFile) {super(jarFile);}

        @Override
        protected boolean accepts(JarEntry jarEntry) {return jarEntry.getName().endsWith(".class");}
    }

    public static class JarNamesIterator extends AbstractIterator<String> {
        private final JarEntryIterator jarEntryIterator;

        public JarNamesIterator(final JarFile jarFile) {jarEntryIterator = new JarEntryIterator(jarFile);}

        @Override
        protected String computeNext() {
            return jarEntryIterator.hasNext() ? jarEntryIterator.next().getName() : endOfData();
        }
    }

    public static class JarStreamIterator extends AbstractIterator<DataInputStream> {
        private final JarClassIterator jarClassIterator;

        public JarStreamIterator(JarFile jarFile) {
            jarClassIterator = new JarClassIterator(jarFile);
        }

        @Override
        protected DataInputStream computeNext() {
            try {
                if (jarClassIterator.hasNext()) {
                    final JarEntry entry = jarClassIterator.next();
                    return new DataInputStream(new BufferedInputStream(
                            jarClassIterator.jarFile.getInputStream(entry)));
                }
            } catch (IOException e) {throw new RuntimeException(e);} //todo: better log

            return endOfData();
        }
    }
}
