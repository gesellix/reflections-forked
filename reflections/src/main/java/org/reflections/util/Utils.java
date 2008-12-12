package org.reflections.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 *
 */
public abstract class Utils {

    public static <T> Set<Class<? extends T>> forNames(final Collection<String> classes) {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>(classes.size());
        for (String aClass : classes) {
            try {result.add((Class<? extends T>) Class.forName(aClass));}
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

    public static Method getMethodFromString(String method) {
        String fullName = method.substring(0, method.indexOf(" "));
        String className = fullName.substring(0,fullName.lastIndexOf("."));
        String methodName = fullName.substring(className.length() + 1);
        String descriptor = method.substring(method.indexOf("(") + 1, method.lastIndexOf(")"));

        List<Class<?>> types = DescriptorHelper.descriptorToTypes(descriptor);
        Class<?>[] parameterTypes = types.toArray(new Class<?>[types.size()]);

        try {
            return Class.forName(className).getMethod(methodName, parameterTypes);
        }
        catch (ClassNotFoundException e) {throw new RuntimeException(e);}
        catch (NoSuchMethodException e) {throw new RuntimeException(e);}
    }

    public static Field getFieldFromString(String field) {
        String className = field.substring(0,field.lastIndexOf("."));
        String fieldName = field.substring(field.lastIndexOf(".") + 1);

        try {
            return Class.forName(className).getDeclaredField(fieldName);
        }
        catch (ClassNotFoundException e) {throw new RuntimeException(e);}
        catch (NoSuchFieldException e) {throw new RuntimeException(e);}
    }

}
