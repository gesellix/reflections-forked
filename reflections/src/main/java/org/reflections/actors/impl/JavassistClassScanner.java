package org.reflections.actors.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import static javassist.Modifier.isAnnotation;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import org.reflections.actors.Scanner;
import org.reflections.helper.Filters;
import static org.reflections.helper.Filters.filter;
import org.reflections.helper.StupidLazyMap;
import org.reflections.helper.UrlIterators;
import org.reflections.helper.DescriptorHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;
import org.reflections.model.meta.*;
import org.reflections.model.meta.meta.FirstClassElement;
import org.reflections.model.meta.meta.BasicElement;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Scans classes input stream using javassist to store class metadata into ClasspathMD
 * Uses given Configuration inclusion/exclusion patterns to filter metadata  
 *
 * @author mamo
 */
public class JavassistClassScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    private static Filters.Filter<String> fqnIncludeExcludePatternFilter;

    public JavassistClassScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
        fqnIncludeExcludePatternFilter = new Filters.IncludeAllExcludeIncludePatternFilter(configuration);
    }

    @SuppressWarnings({"ConstantConditions"})
    public void scan() {
        for (URL url : configuration.getUrls()) {

            final Iterator<DataInputStream> streamIterator = UrlIterators.createStreamIterator(url, Filters.classFileFilter);
            while (streamIterator.hasNext()) {
                DataInputStream dataInputStream = null;
                try {
                    dataInputStream = streamIterator.next();
                    FirstClassElement element = scanClass(dataInputStream);
                    classpathMD.addMetaClass(element);
                }
                catch (IOException e) {throw new RuntimeException(e);} //todo: better log
                finally {try {dataInputStream.close();} catch (IOException e) {/*fuck off*/}}
            }
        }
    }

    private FirstClassElement scanClass(DataInputStream dataInputStream) throws IOException {
        final ClassFile classFile = new ClassFile(dataInputStream);

        final String className = classFile.getName();

        if (!fqnIncludeExcludePatternFilter.accepts(className)) {return null;}

        final FirstClassElement element =
                /*annotation*/ (isAnnotation(classFile.getAccessFlags()) ? annotationsCache.get(className) :
                /*interface*/  classFile.isInterface()                   ? interfacesCache.get(className) :
                /*class*/      /*default*/                                 classesCache.get(className));

        for (ElementTypes elementType : configuration.getElementTypesToScan()) {
            scanMD(classFile, elementType, element);
        }

        return element;
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private StupidLazyMap<String, MetaAnnotation> annotationsCache = new StupidLazyMap<String, MetaAnnotation>() {
        protected MetaAnnotation create(String key) {return new MetaAnnotation(key);}
    };

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private StupidLazyMap<String, MetaInterface> interfacesCache = new StupidLazyMap<String, MetaInterface>() {
        protected MetaInterface create(String key) {return new MetaInterface(key);}
    };

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private StupidLazyMap<String, MetaClass> classesCache = new StupidLazyMap<String, MetaClass>() {
        protected MetaClass create(String key) {return new MetaClass(key);}
    };

    private void scanMD(ClassFile classFile, ElementTypes elementType, FirstClassElement element) {
        switch (elementType) {

            case annotations:
                Set<String> annotationNames = filter(getAnnotations(classFile), fqnIncludeExcludePatternFilter);
                for (String annotationName : annotationNames) {element.addAnnotation(annotationsCache.get(annotationName));}
                break;
            case supertypes:
                //interfaces
                if (element instanceof MetaInterface) {
                    final Set<String> interfacesTypes = filter(asList(classFile.getInterfaces()), fqnIncludeExcludePatternFilter);
                    for (String interfacesType : interfacesTypes) {((MetaInterface) element).addInterface(interfacesCache.get(interfacesType));}
                }

                //supertypes
                if (element instanceof MetaClass) {
                    final String superclass = classFile.getSuperclass();
                    if (fqnIncludeExcludePatternFilter.accepts(superclass)) {
                        ((MetaClass) element).addSuperClasses(classesCache.get(superclass));
                    }
                }
                break;
            case fields:
                if (element instanceof MetaClass) {
                    //noinspection unchecked
                    for (FieldInfo fieldInfo : (List<FieldInfo>) classFile.getFields()) {
                        final MetaField field = (MetaField) new MetaField(element)
                                .setName(fieldInfo.getName())
                                .setType(DescriptorHelper.typeDescriptorToType(fieldInfo.getDescriptor()));

                        if (shouldGetAnnotations()) {
                            annotationNames = filter(getAnnotations(fieldInfo), fqnIncludeExcludePatternFilter);
                            for (String annotationName : annotationNames) {field.addAnnotation(annotationsCache.get(annotationName));}
                        }

                        ((MetaClass) element).addFields(field);
                    }
                }
                break;
            case methods:
                //noinspection unchecked
                for (MethodInfo methodInfo : (List<MethodInfo>) classFile.getMethods()) {
                    final BasicElement method =
                            /*constructor*/ methodInfo.isConstructor() ? new MetaConstructor(element) :
                            /*else method*/                              new MetaMethod(element);

                    if (shouldGetAnnotations()) {
                        annotationNames = filter(getAnnotations(methodInfo), fqnIncludeExcludePatternFilter);
                        for (String annotationName : annotationNames) {
                            method.addAnnotation(annotationsCache.get(annotationName));
                        }
                    }

                    //parameters
                    String[] paramTypeNames = DescriptorHelper.methodDescriptorToParamTypes(methodInfo.getDescriptor());

                    final List<MetaField> parameters = new ArrayList<MetaField>(paramTypeNames.length);
                    if (paramTypeNames.length!=0) {
                        for (String paramTypeName : paramTypeNames) {
                            parameters.add(
                                    (MetaField) new MetaField(element).setType(paramTypeName));
                        }

                        if (shouldGetAnnotations()) {
                            final ParameterAnnotationsAttribute parameterAnnotationsAttribute = (ParameterAnnotationsAttribute) methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag);
                            if (parameterAnnotationsAttribute != null) {
                                final Annotation[][] annotations = parameterAnnotationsAttribute.getAnnotations();
                                int j = 0;
                                for (MetaField parameter : parameters) {
                                    annotationNames = filter(getAnnotationsTypeNames(annotations[j++]), fqnIncludeExcludePatternFilter);
                                    for (String annotationName : annotationNames) {
                                        parameter.addAnnotation(annotationsCache.get(annotationName));
                                    }
                                }
                            }
                        }
                    }

                    if (method instanceof MetaMethod) {
                        final MetaMethod metaMethod = (MetaMethod) method;
                        method.setType(DescriptorHelper.methodDescriptorToType(methodInfo.getDescriptor()));
                        metaMethod.setName(methodInfo.getName());
                        metaMethod.setParameters(parameters);
                        element.addMethod(metaMethod);
                    } else {
                        final MetaConstructor metaConstructor = (MetaConstructor) method;
                        metaConstructor.setType(element.getType());
                        metaConstructor.setParameters(parameters);
                        ((MetaClass) element).addConstructor(metaConstructor);
                    }
                }
                break;

            default:
                //todo: wtf
                throw new UnsupportedOperationException();
        }
    }

    private boolean shouldGetAnnotations() {
        return configuration.getElementTypesToScan().contains(ElementTypes.annotations);
    }

    private static Set<String> getAnnotations(ClassFile classFile) {
        return getAnnotations(classFile.getAttribute(AnnotationsAttribute.visibleTag));
    }

    private Set<String> getAnnotations(FieldInfo field) {
        return getAnnotations(field.getAttribute(AnnotationsAttribute.visibleTag));
    }

    private Set<String> getAnnotations(MethodInfo method) {
        return getAnnotations(method.getAttribute(AnnotationsAttribute.visibleTag));
    }

    private static Set<String> getAnnotations(AttributeInfo attributeInfo) {
        final Set<String> result = Sets.newHashSet();
        if (attributeInfo != null) {
            final Annotation[] annotations = ((AnnotationsAttribute) attributeInfo).getAnnotations();
            for (Annotation annotation : annotations) {
                result.add(annotation.getTypeName());
            }
        }
        return result;
    }

    private static List<String> getAnnotationsTypeNames(Annotation[] annotations ) {
        final List<String> result = Lists.newArrayList();
        for (Annotation annotation : annotations) {
            result.add(annotation.getTypeName());
        }
        return result;
    }
}
