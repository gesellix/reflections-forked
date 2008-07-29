package org.reflections.actors.impl;

import com.google.common.collect.Lists;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import org.reflections.actors.Scanner;
import org.reflections.model.ClassMD;
import org.reflections.model.ClasspathMD;
import org.reflections.model.Configuration;
import org.reflections.model.ElementTypes;
import org.reflections.helper.Logs;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.IteratorFactory;
import org.scannotation.archiveiterator.StreamIterator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author mamo
 */
public class JavaAssistClassScanner implements Scanner {
    private final Configuration configuration;
    private final ClasspathMD classpathMD;

    public JavaAssistClassScanner(Configuration configuration, ClasspathMD classpathMD) {
        this.configuration = configuration;
        this.classpathMD = classpathMD;
    }

//    public void todo_scan() {
//        Iterator<ClasspathIterator.Resource> classIterator = new ClasspathIterator(configuration).getClassesIterator();
//        while (classIterator.hasNext()) {
//            ClasspathIterator.Resource resource = classIterator.next();
//            classpathMD.addClassMD(scan(resource));
//        }
//    }

    private boolean match1(final String name) {
        String filename = name;
        boolean accepts = false;

        if (filename.endsWith(".class")) {
            accepts = true;
            if (filename.startsWith("/")) {
                filename = filename.substring(1);
            }
            filename = filename.replace("/", ".");

            for (String fqnPrefix : configuration.getExcludedFqnPrefixes()) {
                if (filename.startsWith(fqnPrefix)) {
                    accepts = false;
                    break;
                }
            }

            for (String fqnPrefix : configuration.getIncludeFqnPrefixes()) {
                if (filename.startsWith(fqnPrefix)) {
                    accepts = true;
                    break;
                }
            }
        }
        return accepts;
    }

    public void scan() {
        for (URL url : configuration.getUrls()) {

            //todo: avoid scanning urls with post compiled resources, maybe
            //  configuration._postCompiledResources=resources;

            Filter filter = new Filter() {
                public boolean accepts(final String fileName) {
                    String filename = fileName;
                    boolean accepts = false;

                    if (filename.endsWith(".class")) {
                        accepts = true;
                        if (filename.startsWith("/")) {
                            filename = filename.substring(1);
                        }
                        filename = filename.replace("/", ".");

                        for (String fqnPrefix : configuration.getExcludedFqnPrefixes()) {
                            if (filename.startsWith(fqnPrefix)) {
                                accepts = false;
                                break;
                            }
                        }

                        for (String fqnPrefix : configuration.getIncludeFqnPrefixes()) {
                            if (filename.startsWith(fqnPrefix)) {
                                accepts = true;
                                break;
                            }
                        }
                    }
                    return accepts;
                }
            };

            try {
                StreamIterator it = IteratorFactory.create(url, filter);

                InputStream stream;
                while ((stream = it.next()) != null) {
                    ClassMD classMD = scanClassStream(stream);
                    if (classMD==null) {continue;} //todo: remove debug
                    classpathMD.addClassMD(classMD);
                }
            } catch (IOException e) {
                throw new RuntimeException(e); //todo: better exception
            }
        }
    }

    private ClassMD scanClassStream(InputStream inputStream) {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));

        try {
            return scanClass(dataInputStream);
        }
        catch (IOException e) {
            Logs.error("e = " + e);
//            throw new RuntimeException(e); //todo: better log
            return null;
        } finally {
            try {
                dataInputStream.close();
                inputStream.close();
            } catch (IOException e) {/*fuck off*/}
        }
    }

    private ClassMD scanClass(DataInputStream dataInputStream) throws IOException {
        final ClassFile classFile = new ClassFile(dataInputStream);

        ClassMD classMD = new ClassMD(classFile.getName());
        for (ElementTypes elementType : configuration.getElementTypesToScan()) {
            classMD.addMD(elementType, scanMD(classFile, elementType));
        }

        return classMD;
    }

    //todo: extract to strategy
    //todo: use sets or collections
    private static List<String> scanMD(ClassFile classFile, ElementTypes elementType) {
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
                throw new UnsupportedOperationException();
        }
    }

    private static List<String> getAnnotations(ClassFile classFile) {
        List<String> result= Lists.newArrayList();

        AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (visible != null) {
            for (Annotation annotation : visible.getAnnotations()) {
                result.add(annotation.getTypeName());
            }
        }

        return result;
    }

}
