package org.reflections;

import com.google.common.base.Predicates;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/** */
public class VfsTest {

    private void testVfsDir(URL url) {
        Assert.assertNotNull(url);

        Vfs.Dir dir = Vfs.fromURL(url);
        Assert.assertNotNull(dir);

        Iterable<Vfs.File> files = dir.getFiles();
        Vfs.File first = files.iterator().next();
        Assert.assertNotNull(first);

        first.getName();
        try {
            first.openInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dir.close();
    }

    @Test
    public void vfsFromJar() {
        testVfsDir(getSomeJar());
    }

    @Test
    public void vfsFromDir() {
        testVfsDir(getSomeDirectory());
    }

    @Test
    public void vfsFromJarFileUrl() throws MalformedURLException {
        testVfsDir(new URL("jar:file:" + getSomeJar().getPath() + "!/"));
    }

    @Test
    public void findFilesFromEmptyMatch() throws MalformedURLException {
        final URL jar = getSomeJar();
        final Iterable<Vfs.File> files = Vfs.findFiles(java.util.Arrays.asList(jar), Predicates.<Vfs.File>alwaysFalse());
        Assert.assertNotNull(files);
        Assert.assertFalse(files.iterator().hasNext());
    }

//    @Test
    public void vfsFromHttpUrl() throws MalformedURLException {
        Vfs.addDefaultURLTypes(new Vfs.UrlType() {
            public boolean matches(URL url)         {return url.getProtocol().equals("http");}
            public Vfs.Dir createDir(final URL url) {return new HttpDir(url);}
        });

        testVfsDir(new URL("http://mirrors.ibiblio.org/pub/mirrors/maven2/org/slf4j/slf4j-api/1.5.6/slf4j-api-1.5.6.jar"));
    }

    //this is just for the test...
    static class HttpDir implements Vfs.Dir {
        private final File file;
        private final ZipDir zipDir;
        private final String path;

        HttpDir(URL url) {
            this.path = url.toExternalForm();
            try {file = downloadTempLocally(url);}
            catch (IOException e) {throw new RuntimeException(e);}
            zipDir = new ZipDir(file.getAbsolutePath());
        }

        public String getPath() {return path;}
        public Iterable<Vfs.File> getFiles() {return zipDir.getFiles();}
        public void close() {file.delete();}

        private static java.io.File downloadTempLocally(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                java.io.File temp = java.io.File.createTempFile("urlToVfs", "tmp");
                FileOutputStream out = new FileOutputStream(temp);
                DataInputStream in = new DataInputStream(connection.getInputStream());

                int len; byte ch[] = new byte[1024];
                while ((len = in.read(ch)) != -1) {out.write(ch, 0, len);}

                connection.disconnect();
                return temp;
            }

            return null;
        }
    }

    @Test
    public void vfsFromJarWithInnerJars() {
        //todo?
    }

    //
    public URL getSomeJar() {
        Collection<URL> urls = ClasspathHelper.getUrlsForClassloader();
        for (URL url : urls) {
            if (url.getFile().endsWith(".jar")) {
                return url;
            }
        }

        Assert.fail("could not find jar url");
        return null;
    }

    private URL getSomeDirectory() {
        try {
            return new File(ReflectionsTest.getUserDir()).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
