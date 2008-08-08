package org.reflections.model;

import com.google.common.collect.*;

import java.util.*;

/**
 * @author mamo
 */
@SuppressWarnings({"PackageVisibleField"})
public class ClasspathMD {
    Map<String/*class name*/,ClassMD> classesMD = Maps.newHashMap();
    Multimap<String, String> invertedMD = Multimaps.newHashMultimap();

    public Set<String> getClassesKeys() {
        return Sets.newConcurrentHashSet(classesMD.keySet());
    }

    public ClassMD getClassMD(String className) {
        return classesMD.get(className);
    }

    public void addClassMD(ClassMD classMD) {
        if (classMD!=null) {
            classesMD.put(classMD.getName(), classMD);
        }
    }

    public void addClassMD(Set<ClassMD> classMDs) {
        for (ClassMD classMD : classMDs) {
            addClassMD(classMD);
        }
    }

    public void addInvertedMD(String className, Collection<String> elements) {
        for (String element : elements) {
            invertedMD.put(element, className);
        }
    }

    public Collection<String> getInvertedMD(String elementName) {
        return invertedMD.get(elementName);
    }

    public void addClasspathMD(ClasspathMD classpathMD) {
        classesMD.putAll(classpathMD.classesMD);
        invertedMD.putAll(classpathMD.invertedMD);
    }

    public int getClassCount() {
        return classesMD.size();
    }
}