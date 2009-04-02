package org.reflections.scanners;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class MethodAnnotationsScanner extends AbstractScanner {
    public static final String indexName = "MethodAnnotations";

    public void scan(final Object cls) {
		String className = getMetadataAdapter().getClassName(cls);
		List<Object> methods = getMetadataAdapter().getMethods(cls);
		for (Object method : methods) {
			String methodKey = getMetadataAdapter().getMethodKey(method);
			List<String> methodAnnotations = getMetadataAdapter().getMethodAnnotationNames(method);
			for (String methodAnnotation : methodAnnotations) {
				if (accept(methodAnnotation)) {
					populate(methodAnnotation, String.format("%s.%s", className, methodKey));
                }
            }
        }
    }

    public String getIndexName() {
        return indexName;
    }
}
