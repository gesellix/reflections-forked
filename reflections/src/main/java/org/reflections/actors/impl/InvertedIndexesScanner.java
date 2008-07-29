package org.reflections.actors.impl;

import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;
import org.reflections.model.ClassMD;
import org.reflections.model.ElementTypes;
import org.reflections.actors.Scanner;

import java.util.Set;

/**
 * @author mamo
 */
public class InvertedIndexesScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public InvertedIndexesScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }

    public void scan() {
        Set<String> classesKeys = classpathMD.getClassesKeys();
        for (String className : classesKeys) {
            ClassMD classMD = classpathMD.getClassMD(className);

            for (ElementTypes elementType : configuration.getInvertedElementTypes()) {
                classpathMD.addInvertedMD(className, classMD.getMD(elementType));
            }
        }
    }
}
