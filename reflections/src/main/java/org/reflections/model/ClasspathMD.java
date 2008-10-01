package org.reflections.model;

import org.reflections.model.meta.MetaClass;
import org.reflections.model.meta.meta.BasicElement;
import org.reflections.model.meta.meta.FirstClassElement;

import java.util.*;

/**
 * @author mamo
 */
@SuppressWarnings({"PackageVisibleField"})
public class ClasspathMD {
    private final Map<String, FirstClassElement> firstClassElementMap = new HashMap<String, FirstClassElement>();
    private final Map<String,Set<BasicElement>> reverseFirstClassElementsMap = new HashMap<String, Set<BasicElement>>();

    public Collection<FirstClassElement> getTypes() {
        return firstClassElementMap.values();
    }

    public FirstClassElement getType(String className) {
        return firstClassElementMap.get(className);
    }

    public void addMetaClass(FirstClassElement element) {
        if (element != null) {
            firstClassElementMap.put(element.getType(), element);
        }
    }

    public void addMetaClasses(Set<MetaClass> aClasses) {
        for (MetaClass aClass : aClasses) {
            addMetaClass(aClass);
        }
    }

    public Set<BasicElement> getReverseFirstClassElements(String elementName) {
        return reverseFirstClassElementsMap.get(elementName);
    }

    public void mergeClasspathMD(ClasspathMD classpathMD) {
        firstClassElementMap.putAll(classpathMD.firstClassElementMap);
        reverseFirstClassElementsMap.putAll(classpathMD.reverseFirstClassElementsMap);
    }

    public int getClassCount() {
        return firstClassElementMap.size();
    }

    public void addReverseMD(FirstClassElement element1, BasicElement element2) {
        final String elementName = element1.getType();

        if (!reverseFirstClassElementsMap.containsKey(elementName)) {
            reverseFirstClassElementsMap.put(elementName,new HashSet<BasicElement>());
        }

        reverseFirstClassElementsMap.get(elementName).add(element2);
    }
}