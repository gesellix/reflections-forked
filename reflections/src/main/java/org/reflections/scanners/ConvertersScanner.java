package org.reflections.scanners;

import org.reflections.util.DescriptorHelper;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class ConvertersScanner extends AbstractScanner {
    private final Class<?> from;
    private final Class<?> to;
    public static final String indexName = "Converters";

    public ConvertersScanner(final Class<?> from, final Class<?> to) {
        this.from = from;
        this.to = to;
    }

    public void scan(final Object cls) {
        List<Object> methods = getMetadataAdapter().getMethods(cls);
        for (Object method : methods) {
            String returnTypeName = getMetadataAdapter().getReturnTypeName(method);
            List<String> parameterNames = getMetadataAdapter().getParameterNames(method);

            if (parameterNames.size()==1) {
                Class<?> returnType = DescriptorHelper.typeNameToType(returnTypeName);
                Class<?> firstParameter = DescriptorHelper.typeNameToType(parameterNames.get(0));

                if (firstParameter.equals(from) && returnType.equals(to)) {
                    String className = getMetadataAdapter().getClassName(cls);
                    String methodKey = getMetadataAdapter().getMethodKey(method);

                    populate(getConverterKey(from, to), String.format("%s.%s", className, methodKey));
                }
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
