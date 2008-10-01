//package org.reflections.actors.impl;
//
//import com.google.common.base.Function;
//import com.google.common.base.Nullable;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//import org.reflections.actors.Scanner;
//import org.reflections.helper.Filters;
//import static org.reflections.helper.Filters.filterAll;
//import org.reflections.helper.Logs;
//import org.reflections.helper.UrlIterators;
//import org.reflections.model.*;
//import org.reflections.model.meta.Meta;
//
//import java.lang.annotation.MetaAnnotation;
//import java.lang.reflect.AnnotatedElement;
//import java.net.URL;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.List;
//
///**
// */
//public class ReflectionClassScanner implements Scanner {
//    private final Configuration configuration;
//    private final ClasspathMD classpathMD;
//
////    private final URLClassLoader classLoader;
//    private static Filters.Filter<String> fqnIncludeExcludePatternFilter;
//
//    public ReflectionClassScanner(Configuration configuration, ClasspathMD classpathMD) {
//        this.configuration = configuration;
//        this.classpathMD = classpathMD;
//        fqnIncludeExcludePatternFilter =new Filters.IncludeAllExcludeIncludePatternFilter(configuration);
////        classLoader = new URLClassLoader((URL[]) configuration.getUrls().toArray());
//    }
//
//
//    public void scan() {
//
//        for (URL url : configuration.getUrls()) {
//            final Iterator<String> namesIterator = UrlIterators.createNamesIterator(url,Filters.classFileFilter,fqnIncludeExcludePatternFilter);
//            while (namesIterator.hasNext()) {
//                String className = namesIterator.next();
//                try {
//                    final MetaClass aClass = java.lang.MetaClass.forName(className);
//                    Meta.MetaClass metaClass = scanClass(aClass);
//                    classpathMD.addMetaClass(metaClass);
//
//                } catch (ClassNotFoundException e) {
//                    Logs.error(String.format("could not find class %s in classloader",className));
//                }
////                classLoader.
//            }
//        }
//    }
//
//    private Meta.MetaClass scanClass(MetaClass aClass) {
//        final String className = aClass.getName();
//
//        Meta.MetaClass metaClass = Meta.Builder.build(Meta.MetaClass.class,className);
//        for (ElementTypes elementType : configuration.getElementTypesToScan()) {
//            scanMD(aClass, elementType,metaClass);
//        }
//
//        return metaClass;
//    }
//
//    private void scanMD(MetaClass aClass, ElementTypes elementType, Meta.MetaClass metaClass) {
//        switch (elementType) {
//
//            case annotations:
//                ((Meta.MetaClassImpl) metaClass).addAnnotations((Meta.MetaAnnotation[]) getAnnotations(aClass).toArray());
//                break;
//
//            case fields:
//                final java.lang.reflect.MetaField[] declaredFields = aClass.getDeclaredFields();
//                for (java.lang.reflect.MetaField field : declaredFields) {
//                    final Meta.MetaField metaField = Meta.Builder.build(Meta.MetaField.class,field.getType().getName());
//                    ((Meta.MetaFieldImpl)metaField).setName(field.getName());
//
//                    
//                    ((Meta.MetaFieldImpl) metaField).addAnnotations((Meta.MetaAnnotation[]) getAnnotations(field).toArray());
//                    fieldMD.addAnnotations(getAnnotations(field));
//                    classMD.addFieldMD(fieldMD);
//                }
//                break;
//
//            case methods:
//                final java.lang.reflect.MetaMethod[] methods = aClass.getDeclaredMethods();
//                for (java.lang.reflect.MetaMethod method : methods) {
//                    final Meta.MetaMethod method = new Meta.MetaMethod(method.getName(), method.getReturnType().getName());
//                    methodMD.addAnnotations(getAnnotations(method));
//
//                    final MetaClass[] parameterTypes = method.getParameterTypes();
//                    final MetaAnnotation[][] parameterAnnotations = method.getParameterAnnotations();
//                    for (int i = 0; i < parameterTypes.length; i++) {
//                        final MetaClass parameterType = parameterTypes[i];
//                        final Meta.MetaField parameter = new Meta.MetaField(null, parameterType.getName());
//                        parameter.addAnnotations(getAnnotationsTypeNames(parameterAnnotations[i]));
//                        methodMD.addParameterMD(parameter);
//                    }
//
//                    classMD.addMethodMD(methodMD);
//                }
//                break;
//
//            case interfaces:
//                final Iterable<String> interfaces = Iterables.transform(Iterables.cycle(aClass.getInterfaces()), new Function<MetaClass, String>() {
//                    public String apply(@Nullable MetaClass from) {
//                        return from.getName();
//                    }
//                });
//
//                classMD.addMD(elementType, filterAll(interfaces, fqnIncludeExcludePatternFilter));
//                break;
//
//            case supertypes:
//                final MetaClass superclass = aClass.getSuperclass();
//                if (superclass != null && fqnIncludeExcludePatternFilter.accepts(superclass.getName())) {
//                    classMD.addMD(elementType, superclass.getName());
//                }
//                break;
//
//        }
//
//    }
//
//    private List<String> getAnnotations(AnnotatedElement element) {
//        final MetaAnnotation[] declaredAnnotations = element.getDeclaredAnnotations();
//        return getAnnotationsTypeNames(declaredAnnotations);
//    }
//
//    private List<String> getAnnotationsTypeNames(MetaAnnotation[] declaredAnnotations) {
//        final List<String> result = Lists.newArrayList();
//        for (MetaAnnotation annotation : declaredAnnotations) {
//            result.add(annotation.annotationType().getName());
//        }
//        return result;
//    }
//}
