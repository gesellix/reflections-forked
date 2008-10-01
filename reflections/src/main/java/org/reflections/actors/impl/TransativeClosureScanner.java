package org.reflections.actors.impl;

import org.reflections.actors.Scanner;
import org.reflections.helper.RelaxedHashSet;
import org.reflections.helper.StupidLazyMap;
import org.reflections.model.ClasspathMD;
import org.reflections.model.meta.*;
import org.reflections.model.meta.MetaClass;
import org.reflections.model.meta.meta.FirstClassElement;
import org.reflections.model.meta.meta.AnnotatedElement;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    @SuppressWarnings({"RedundantCast"})
    public void scan() {
        for (FirstClassElement element : classpathMD.getTypes()) {

            element.addAnnotations(annotationsCache.get(element));

            if (element instanceof MetaInterface) {
                ((MetaInterface) element).addInterfaces(interfacesCache.get((MetaInterface) element));
            }

            if (element instanceof MetaClass) {
                ((MetaClass) element).addSuperClasses(superTypesCache.get((MetaClass) element));
            }
        }
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<MetaClass, Collection<MetaClass>> superTypesCache =
            new StupidLazyMap<MetaClass, Collection<MetaClass>>() {
                @Override
                protected Collection<MetaClass> create(MetaClass metaClass) {
                    Collection<MetaClass> superTypes = RelaxedHashSet.create();

                    if (metaClass != null) { //todo: try to avoid null
                        for (MetaClass superClass : metaClass.getSuperClasses()) {
                            if (superClass == null) {continue;} //top most type - Object todo try to avoid null
                            superTypes.add(superClass);
                            //add supertypes from each supertype
                            superTypes.addAll(superTypesCache.get(superClass));
                        }
                    }
                    return superTypes;
                }
            };

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<MetaInterface, Collection<MetaInterface>> interfacesCache =
            new StupidLazyMap<MetaInterface, Collection<MetaInterface>>() {
                @SuppressWarnings({"RedundantCast"}) @Override
                protected Collection<MetaInterface> create(MetaInterface anInterface) {
                    Set<MetaInterface> interfaces = RelaxedHashSet.create();

                    if (anInterface != null) { //todo: try to avoid null
                        for (MetaInterface metaInterface : anInterface.getInterfaces()) {
                            if (metaInterface == null) {continue;} //top most type - Object todo try to avoid null
                            interfaces.add(metaInterface);
                            //add interfaces from each interface
                            interfaces.addAll(interfacesCache.get(metaInterface));
                        }

                        //add interfaces from superclasses
                        if (anInterface instanceof MetaClass) {
                            for (MetaClass superClass : superTypesCache.get(((MetaClass) anInterface))) {
                                interfaces.addAll(interfacesCache.get(((MetaInterface) superClass)));
                            }
                        }
                    }
                    return interfaces;
                }
            };

    //todo: this is because ClasspathMD's classMDs could be scanned either by javaAssist or conventional reflection, and the results must be the same
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final Map<AnnotatedElement, Set<MetaAnnotation>> annotationsCache =
            new StupidLazyMap<AnnotatedElement, Set<MetaAnnotation>>() {
                @SuppressWarnings({"RedundantCast"}) @Override
                protected Set<MetaAnnotation> create(AnnotatedElement annotatedElement) {
                    Set<MetaAnnotation> resultAnnotations = RelaxedHashSet.create();
                    if (annotatedElement != null) { //todo: try to avoid null

                        //add meta annotations
                        for (MetaAnnotation anAnnotation : annotatedElement.getAnnotations()) {
                            resultAnnotations.add(anAnnotation); //todo: remove inclusive add
                            resultAnnotations.addAll(annotationsCache.get(anAnnotation));
                        }

                        //add annotations inherited from supertypes
                        //todo: should consider @Inherited?
                        if (annotatedElement instanceof MetaClass) {
                            for (MetaClass superType : superTypesCache.get(((MetaClass) annotatedElement))) {
                                resultAnnotations.addAll(annotationsCache.get(superType));
                            }
                        }

                        //add annotations inhertied from interfaces
                        //todo: should consider @Inherited?
                        if (annotatedElement instanceof MetaInterface) {
                            for (MetaInterface anInterface : interfacesCache.get(((MetaInterface) annotatedElement))) {
                                resultAnnotations.addAll(annotationsCache.get(anInterface));
                            }
                        }
                    }
                    return resultAnnotations;
                }
            };
}

