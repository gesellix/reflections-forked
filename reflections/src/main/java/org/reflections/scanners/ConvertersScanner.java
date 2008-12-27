package org.reflections.scanners;

import org.reflections.util.DescriptorHelper;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class ConvertersScanner extends AbstractScanner {
    public static final String indexName = "Converters";

    public void scan(final Object cls) {
        List<Object> methods = getMetadataAdapter().getMethods(cls);
        for (Object method : methods) {
            String returnTypeName = getMetadataAdapter().getReturnTypeName(method);
            List<String> parameterNames = getMetadataAdapter().getParameterNames(method);

            if (parameterNames.size() == 1) {
                Class<?> from = DescriptorHelper.typeNameToType(parameterNames.get(0));
                Class<?> to = DescriptorHelper.typeNameToType(returnTypeName);

                String className = getMetadataAdapter().getClassName(cls);
                String methodKey = getMetadataAdapter().getMethodKey(method);

                populate(getConverterKey(from, to), String.format("%s.%s", className, methodKey));
            }
        }
    }

    public String getIndexName() {
        return indexName;
    }

    public static String getConverterKey(Class<?> from, Class<?> to) {
        return from + " -> " + to;
    }
}
