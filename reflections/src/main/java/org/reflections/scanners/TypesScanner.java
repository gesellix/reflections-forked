package org.reflections.scanners;

import com.google.common.collect.Lists;
import org.reflections.ReflectionsException;
import org.reflections.serializers.JavaCodeSerializer;
import org.reflections.vfs.Vfs;

import java.util.List;

/** scans classes and stores fqn as key and full path as value */
public class TypesScanner extends AbstractScanner {
    public boolean acceptsInput(String file) {
        return file.endsWith(".class") && !file.endsWith("package-info.class"); //classes only
    }

    //duplicated from AbstractScanner
    public void scan(Vfs.File file) {
        try {
            Object cls = getMetadataAdapter().getOfCreateClassObject(file);
            scan(cls, file);
        } catch (Exception e) {
            throw new ReflectionsException("could not create class file from " + file.getName(), e);
        }
    }

    private void scan(Object cls, Vfs.File file) {
        //avoid scanning JavaCodeSerializer outputs
        //noinspection unchecked
        if (TypesScanner.isJavaCodeSerializer(getMetadataAdapter().getInterfacesNames(cls))) return;

        //noinspection unchecked
        String className = getMetadataAdapter().getClassName(cls);

        getStore().put(className, className);
    }

    @Override
    public void scan(Object cls) {
        throw new UnsupportedOperationException("should not get here");
    }

    //
    public static boolean isJavaCodeSerializer(List<String> interfacesNames) {
        return interfacesNames.size() == 1 && javaCodeSerializerInterfaces.contains(interfacesNames.get(0));
    }

    private final static List<String> javaCodeSerializerInterfaces = Lists.newArrayList(
            JavaCodeSerializer.IElement.class.getName(),
            JavaCodeSerializer.IPackage.class.getName(),
            JavaCodeSerializer.IClass.class.getName(),
            JavaCodeSerializer.IField.class.getName(),
            JavaCodeSerializer.IMethod.class.getName()
    );
}
