package org.reflections.actors.impl;

import com.thoughtworks.xstream.XStream;
import org.reflections.actors.Marshaller;
import org.reflections.model.ClasspathMD;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

/**
 * @author mamo
 */
public class XmlMarshaller implements Marshaller {
    private final ClasspathMD classpathMD;
    private XStream xStream = new XStream();

    public XmlMarshaller(ClasspathMD classpathMD) {
        this.classpathMD = classpathMD;
    }

    public void save(String destination) {
        String xml = xStream.toXML(classpathMD);

        try {
            safeWriteFile(xml,new File(destination));
        } catch (IOException e) {
            throw new RuntimeException(String.format("could not save file %s", destination), e);
        }
    }

    private void safeWriteFile(String fileContent, File destFile) throws IOException {
        FileWriter writer = null;
        try {
            if (destFile.getParentFile() !=null) {destFile.getParentFile().mkdirs();}
            
            writer = new FileWriter(destFile);
            writer.write(fileContent);
//            getLog().info(format("Scannotations plugin created file %s", destFile));
        } finally {
            try {
                //noinspection ConstantConditions
                writer.close();
            } catch (Exception e) {/*fuck off*/}
        }
    }

    public void load(String resource) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final InputStream stream = loader.getResourceAsStream(resource);

        ClasspathMD newClasspathMD = (ClasspathMD) xStream.fromXML(stream);

        classpathMD.addClasspathMD(newClasspathMD);
    }
}
