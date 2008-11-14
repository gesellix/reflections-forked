package org.reflections.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public abstract class Utils {

    public static Set<Class<?>> forNames(final Collection<String> classes) {
        Set<Class<?>> result = new HashSet<Class<?>>(classes.size());
        for (String aClass : classes) {
            try {result.add(Class.forName(aClass));}
            catch (ClassNotFoundException e) {throw new RuntimeException(e);}
        }
        return result;
    }

    public static void safeWriteFile(String fileContent, File destFile) throws IOException {
        FileWriter writer = null;
        try {
            if (destFile.getParentFile() != null) {destFile.getParentFile().mkdirs();}

            writer = new FileWriter(destFile);
            writer.write(fileContent);
        } finally {
            try {
                //noinspection ConstantConditions
                writer.close();
            } catch (Exception e) {/*fuck off*/}
        }
    }

    public static ClassLoader getEffectiveClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
