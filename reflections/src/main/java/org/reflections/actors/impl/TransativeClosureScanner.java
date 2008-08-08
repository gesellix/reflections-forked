package org.reflections.actors.impl;

import org.reflections.model.ClassMD;
import org.reflections.model.ClasspathMD;
import static org.reflections.model.ElementTypes.*;
import org.reflections.helper.StupidLazyMap;
import org.reflections.helper.RelaxedHashSet;
import org.reflections.actors.Scanner;

import java.util.*;

/**
 * Computes the transitive closure on the supertypes, interfaces and (meta) annotations hierarchies
 *
 * @author mamo
 */
public class TransativeClosureScanner implements Scanner {
    private final ClasspathMD classpathMD;

    public TransativeClosureScanner(ClasspathMD classpathMD) {
        this.classpathMD = classpathMD;
    }

    public void scan() {
        Set<String> classesKeys = classpathMD.getClassesKeys();
        for (String className : classesKeys) {
            ClassMD classMD = classpathMD.getClassMD(className);

            classMD.addMD(supertypes, superTypesCache.get(className));
            classMD.addMD(interfaces, interfacesCache.get(className));
            classMD.addMD(annotations, annotationsCache.get(className));
        }
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<String, Set<String>> superTypesCache =
            new StupidLazyMap<String, Set<String>>() {
                @Override
                protected Set<String> create(String className) {
                    ClassMD classMD = classpathMD.getClassMD(className);
                    Set<String> superTypes = RelaxedHashSet.create();

                    if (classMD != null) {
                        for (String superType : classMD.getMD(supertypes)) {
                            if (superType == null) {continue;} //top most type - Object
                            superTypes.add(superType);
                            Set<String> strings = superTypesCache.get(superType);
                            superTypes.addAll(strings);
                        }
                    }
                    return superTypes;
                }
            };

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<String, Set<String>> interfacesCache =
            new StupidLazyMap<String, Set<String>>() {
                @Override
                protected Set<String> create(String className) {
                    ClassMD classMD = classpathMD.getClassMD(className);
                    Set<String> resultInterfaces = RelaxedHashSet.create();

                    if (classMD != null) {
                        for (String anInterface : classMD.getMD(interfaces)) {
                            resultInterfaces.add(anInterface);
                            resultInterfaces.addAll(interfacesCache.get(anInterface));
                        }

                        //add interfaces from superclasses
                        for (String superClass : superTypesCache.get(className)) {
                            resultInterfaces.addAll(interfacesCache.get(superClass));
                        }
                    }

                    return resultInterfaces;
                }
            };

    //todo: should consider @Inherited?
    //todo: this is because ClasspathMD's classMDs could be scanned either by javaAssist or conventional reflection, and the results must be the same
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<String, Set<String>> annotationsCache =
            new StupidLazyMap<String, Set<String>>() {
                private final Stack<String> classesStack = new Stack<String>();

                @Override
                protected Set<String> create(String className) {
                    ClassMD classMD = classpathMD.getClassMD(className);

                    classesStack.push(className);

                    Set<String> resultAnnotations = RelaxedHashSet.create();

                    if (classMD != null) {

                        Collection<String> classAnnotations = classMD.getMD(annotations);
                        classAnnotations.removeAll(classesStack); //avoid circularity

                        for (String anAnnotation : classAnnotations) {
                            resultAnnotations.add(anAnnotation);
                            resultAnnotations.addAll(annotationsCache.get(anAnnotation));
                        }
                        for (String superType : superTypesCache.get(className)) {
                            resultAnnotations.addAll(annotationsCache.get(superType));
                        }
                        for (String anInterface : interfacesCache.get(className)) {
                            resultAnnotations.addAll(annotationsCache.get(anInterface));
                        }
                    }

                    classesStack.pop();
                    return resultAnnotations;
                }
            };

}

