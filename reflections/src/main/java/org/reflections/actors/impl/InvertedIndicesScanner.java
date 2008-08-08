package org.reflections.actors.impl;

import org.reflections.model.Configuration;
import org.reflections.model.ClasspathMD;
import org.reflections.model.ClassMD;
import org.reflections.model.ElementTypes;
import org.reflections.actors.Scanner;

import java.util.Set;

/**
 * Generates the inverted metadata indices, not in the key of classes, but in the key of classes metadata
 * That is, for example, enalbes to ask questions like
 *    what classes are annotated with a specific annotation
 *    what are the extending types of a specific type
 *
 * @author mamo
 */
public class InvertedIndicesScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public InvertedIndicesScanner(Configuration configuration, ClasspathMD classpathMD) {
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
