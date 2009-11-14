package org.reflections.scanners;

import com.google.common.collect.Lists;
import org.reflections.serializers.JavaCodeSerializer;

import java.util.List;

/** scans fields and methods and stores fqn as key and elements as values */
@SuppressWarnings({"unchecked"})
public class TypeElementsScanner extends AbstractScanner {
    public void scan(Object cls) {
        //avoid scanning JavaCodeSerializer outputs
        if (TypesScanner.isJavaCodeSerializer(getMetadataAdapter().getInterfacesNames(cls))) return; 

        String className = getMetadataAdapter().getClassName(cls);

        for (Object field : getMetadataAdapter().getFields(cls)) {
            String fieldName = getMetadataAdapter().getFieldName(field);
            getStore().put(className, fieldName);
        }

        for (Object method : getMetadataAdapter().getMethods(cls)) {
            getStore().put(className, getMetadataAdapter().getMethodKey(cls, method));
        }
    }
}
