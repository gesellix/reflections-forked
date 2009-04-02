package org.reflections.util;

import static org.reflections.util.ReflectionUtil.resolveClass;

import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class DescriptorHelper {
    /**
     * I[Ljava.lang.String; -> I, [Ljava.lang.String;
     */
    public static List<String> splitDescriptor(final String descriptor) {
        List<String> result = new ArrayList<String>();

        int i1 = 0;
        while (i1 < descriptor.length()) {
            int i2 = i1;
            while (descriptor.charAt(i2) == '[') {
                i2++;
            }
            char rawType = descriptor.charAt(i2++);
            if (rawType == 'L') {
                i2 = descriptor.indexOf(";", i2) + 1;
            }

            String type = descriptor.substring(i1, i2);

            result.add(type);

            i1 = i2;
        }

        return result;
    }

    /**
     * I[Ljava.lang.String; -> int, java.lang.String[]
     */
    public static List<Class<?>> descriptorToTypes(final String descriptor) {
        List<Class<?>> result = new ArrayList<Class<?>>();

        List<String> typeNames = splitDescriptor(descriptor);

        for (String type : typeNames) {
            Class<?> aClass = typeNameToType(type);

            result.add(aClass);
        }

        return result;
    }

    /**
     * I -> Integer.TYPE ; [Ljava.lang.String; -> java.lang.String[]
     */
    public static Class<?> typeNameToType(final String type) {
        Class<?> aClass;

        if (type.startsWith("[")) {
            //array
            String type1 = type.replace("/","."); //still might be an object
			aClass = resolveClass(type1);
		} else {
            if (type.startsWith("L")) {
                //non array object
                String type1 = type.substring(1,type.indexOf(";")).replace("/",".");
				aClass  = resolveClass(type1);
			} else {
                //primitive type
                aClass = simplePrimitiveToType(type.charAt(0));
            }
        }

        return aClass;
    }

    /**
     * method (I[Ljava.lang.String;)Ljava.lang.Object; -> I, [Ljava.lang.String;
     */
    public static List<String> methodDescriptorToParameterNameList(final String descriptor) {
        return splitDescriptor(
                descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")")));
    }

    /**
     * method (I[Ljava.lang.String;)Ljava.lang.Object; -> Ljava.lang.Object;
     */
    public static String methodDescriptorToReturnTypeName(final String descriptor) {
        return splitDescriptor(
                descriptor.substring(descriptor.lastIndexOf(")") + 1))
                .get(0);
    }

    /**
     * I -> Integer.TYPE ; V -> Void.TYPE
     */
    public static Class<?> simplePrimitiveToType(char rawType) {
        return 'Z' == rawType ? Boolean.TYPE :
               'C' == rawType ? Character.TYPE :
               'B' == rawType ? Byte.TYPE :
               'S' == rawType ? Short.TYPE :
               'I' == rawType ? Integer.TYPE :
               'J' == rawType ? Long.TYPE :
               'F' == rawType ? Float.TYPE :
               'D' == rawType ? Double.TYPE :
               'V' == rawType ? Void.TYPE :
               /*error*/      null;
    }

    /**
     * java.lang.String -> java/lang/String.class
     */
    public static String classNameToResourceName(final String className) {
        return qNameToResourceName(className) + ".class";
    }

    /**
     * java.lang.String -> java/lang/String
     */
    public static String qNameToResourceName(String qName) {
        return qName.replace(".", "/");
    }

}
