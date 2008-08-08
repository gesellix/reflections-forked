package org.reflections.model;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;

/**
 * @author mamo
 */
public class ClassMD {
    private final String name;
    private Multimap<ElementTypes, String> elementTypesMap = Multimaps.newHashMultimap();

    public ClassMD(String name) {this.name = name;}

    public String getName() {return name;}

    public void addMD(ElementTypes elementType, String element) {
        elementTypesMap.put(elementType, element);
    }

    public void addMD(ElementTypes elementType, Iterable<String> elements) {
        elementTypesMap.putAll(elementType, elements);
    }

    public Collection<String> getMD(ElementTypes elementType) {
        return elementTypesMap.get(elementType);
    }
}
