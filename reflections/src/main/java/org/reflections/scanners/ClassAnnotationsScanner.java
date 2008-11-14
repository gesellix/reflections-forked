package org.reflections.scanners;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class ClassAnnotationsScanner extends AbstractScanner {
    public static final String indexName = "ClassAnnotations";

    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);

        List<String> annotationTypes = getMetadataAdapter().getClassAnnotationNames(cls);
        for (String annotationType : annotationTypes) {
            if (accept(annotationType)) {
                populate(annotationType, className);
            }
        }
    }

    public String getIndexName() {
        return indexName;
    }
}
