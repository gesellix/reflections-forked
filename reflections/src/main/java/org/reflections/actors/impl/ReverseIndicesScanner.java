package org.reflections.actors.impl;

import org.reflections.actors.Scanner;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;
import org.reflections.model.meta.*;
import org.reflections.model.meta.meta.FirstClassElement;

import java.util.Collection;

/**
 * Generates the inverted metadata indices, not in the key of classes, but in the key of classes metadata
 * That is, for example, enalbes to ask questions like
 *    what classes are annotated with a specific annotation
 *    what are the extending types of a specific type
 *
 * @author mamo
 */
public class ReverseIndicesScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public ReverseIndicesScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }

    public void scan() {
        for (FirstClassElement element : classpathMD.getTypes()) {

            for (ElementTypes elementType : configuration.getReverseElementTypes()) {
                switch (elementType) {
                    case annotations:
                        Collection<MetaAnnotation> annotations = element.getAnnotations();
                        for (MetaAnnotation annotation : annotations) {
                            classpathMD.addReverseMD(annotation, element);
                        }
                        break;
                    case supertypes:
                        //interfaces
                        if (element instanceof MetaInterface) {
                            final Collection<MetaInterface> interfaces = ((MetaInterface) element).getInterfaces();
                            for (MetaInterface anInterface : interfaces) {
                                classpathMD.addReverseMD(anInterface, element);
                            }
                        }

                        //supertypes
                        if (element instanceof MetaClass) {
                            final Collection<MetaClass> superClasses = ((MetaClass) element).getSuperClasses();
                            for (MetaClass superClass : superClasses) {
                                classpathMD.addReverseMD(superClass, element);
                            }
                        }
                        break;
                    case fields:
                        if (element instanceof MetaClass) {
                            for (MetaField field : ((MetaClass) element).getFields()) {
                                //annotations to field
                                for (MetaAnnotation annotation : field.getAnnotations()) {
                                    classpathMD.addReverseMD(annotation, field);
                                }
                            }
                        }
                        break;
                    case methods:
                        for (MetaMethod method : element.getMethods()) {
                            //annotations to method
                            for (MetaAnnotation annotation : method.getAnnotations()) {
                                classpathMD.addReverseMD(annotation, method);
                            }
                        }
                        break;
                }
            }
        }
    }
}
