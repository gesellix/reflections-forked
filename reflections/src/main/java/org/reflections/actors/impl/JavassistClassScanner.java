package org.reflections.actors.impl;

import static com.google.common.collect.Lists.newArrayList;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import org.reflections.actors.Scanner;
import org.reflections.helper.Filters;
import org.reflections.helper.Logs;
import org.reflections.helper.UrlIterators;
import org.reflections.model.ClassMD;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;

/**
 * Scans classes input stream using javassist to store class metadata into ClasspathMD
 * Uses given Configuration inclusion/exclusion patterns to filter metadata  
 *
 * @author mamo
 */
public class JavassistClassScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    private static Filters.Filter<String> includeExcludePatternFilter;

    public JavassistClassScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
        includeExcludePatternFilter=new Filters.IncludeAllExcludeIncludePatternFilter(configuration);
    }

    public void scan() {
        for (URL url : configuration.getUrls()) {

            final Iterator<DataInputStream> streamIterator = UrlIterators.createStreamIterator(url, Filters.classFileFilter);
            while (streamIterator.hasNext()) {
                DataInputStream dataInputStream = streamIterator.next();

                ClassMD classMD = scanClassStream(dataInputStream);
                try {dataInputStream.close();} catch (IOException e) {/*fuck off*/}

                classpathMD.addClassMD(classMD);
            }
        }
    }

    private ClassMD scanClassStream(DataInputStream dataInputStream) {
        try {
            return scanClass(dataInputStream);
        }
        catch (IOException e) {
            Logs.error("e = " + e);
//            throw new RuntimeException(e); //todo: better log
            return null;
        }
    }

    private ClassMD scanClass(DataInputStream dataInputStream) throws IOException {
        final ClassFile classFile = new ClassFile(dataInputStream);

        final String className = classFile.getName();

        if (!includeExcludePatternFilter.accepts(className)) {return null;}

        ClassMD classMD = new ClassMD(className);
        for (ElementTypes elementType : configuration.getElementTypesToScan()) {
            final Iterable<String> md = scanMD(classFile, elementType);
            final Iterable<String> iterable = Filters.filterAll(md,includeExcludePatternFilter);
            classMD.addMD(elementType, iterable);
        }

        return classMD;
    }

    private Iterable<String> scanMD(ClassFile classFile, ElementTypes elementType) {
        switch (elementType) {

            case annotations:
                return getAnnotations(classFile);
            case interfaces:
                return asList(classFile.getInterfaces());
            case supertypes:
                return asList(classFile.getSuperclass());

            case fieldAnnotations:
            case fields:
            case methodAnnotations:
            case methods:
            case parameterAnnotations:
            default:
                //todo: impl
                throw new UnsupportedOperationException();
        }
    }

    private static List<String> getAnnotations(ClassFile classFile) {
        List<String> result = newArrayList();

        AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (visible != null) {
            for (Annotation annotation : visible.getAnnotations()) {
                result.add(annotation.getTypeName());
            }
        }

        return result;
    }

}
