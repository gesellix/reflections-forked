package org.reflections.scanners;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class FieldAnnotationsScanner extends AbstractScanner {
    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);

        List<Object> fields = getMetadataAdapter().getFields(cls);
        for (Object field : fields) {
            String fieldName = getMetadataAdapter().getFieldName(field);
            List<String> fieldAnnotations = getMetadataAdapter().getFieldAnnotationNames(field);
            for (String fieldAnnotation : fieldAnnotations) {
                if (accept(fieldAnnotation)) {
                    populate(fieldAnnotation, String.format("%s.%s", className, fieldName));
                }
            }
        }
    }

    public String getIndexName() {
        return "FieldAnnotations";
    }
}
