package org.reflections.scanners;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class SubTypesScanner extends AbstractScanner {
    public static final String indexName = "SubTypes";

    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);

        String superclass = getMetadataAdapter().getSuperclassName(cls);
        List<String> interfaces = getMetadataAdapter().getInterfacesNames(cls);

        if (accept(superclass)) {
            populate(superclass, className);
        }
        
        for (String anInterface : interfaces) {
            if (accept(anInterface)) {
                populate(anInterface, className);
            }
        }
    }

    public String getIndexName() {
        return indexName;
    }
}
