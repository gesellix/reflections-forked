package org.reflections.helper;

/**
 * @author mamo
 */
public abstract class DescriptorHelper {


    public static String className(Class<?> aClass) {
        String className = aClass.getName();
        if (className.startsWith("[")) {
            className = typeDescriptorToType(className);
        }

        return className;
    }

    public static String typeDescriptorToType(String typeDescriptor) {
        int c=0;
        String array = typeDescriptor.contains("[") ? typeDescriptor.substring(c, (c = typeDescriptor.lastIndexOf("[")+1)).replace("[","[]") : "";
        Character rawType = typeDescriptor.charAt(c++);
        String object = typeDescriptor.substring(c).replace("/",".").replace(";","");

        String type =
                'L'==rawType ? object :
                 primitiveTypeDescriptorToType(rawType);

        return type+array;
    }

    //()[Ljava.lang.Object  -->  java.lang.Object[]
    public static String methodDescriptorToType(String descriptor) {
        String typeDescriptor = descriptor.substring(descriptor.lastIndexOf(")")+1);
        String type = typeDescriptorToType(typeDescriptor);

        return type;
    }

    //(Ljava.lang.Object; [Ljava.lang.Object;)V  -->  (Object someObject, Object[] someArray)

    public static String[] methodDescriptorToParamTypes(String descriptor) {
        final String parametersDescriptor = descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")"));
        
        if (parametersDescriptor.length()==0) {return new String[0];}

        String[] parameterDescriptors = parametersDescriptor.split(";");
        String[] parameters = new String[parameterDescriptors.length];
        for (int i=0;i<parameterDescriptors.length;i++) {
            parameters[i] = typeDescriptorToType(parameterDescriptors[i]);
        }

        return parameters;
    }

    public static String primitiveTypeDescriptorToType(char rawType) {
        return 'Z'==rawType ? "boolean" :
               'C'==rawType ? "char" :
               'B'==rawType ? "byte" :
               'S'==rawType ? "short" :
               'I'==rawType ? "int" :
               'J'==rawType ? "long" :
               'F'==rawType ? "float" :
               'D'==rawType ? "double" :
               'V'==rawType ? "void" :
               /*error*/      null;
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

}
