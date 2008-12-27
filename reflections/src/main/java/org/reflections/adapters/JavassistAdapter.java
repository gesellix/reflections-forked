package org.reflections.adapters;

import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import org.apache.log4j.Logger;
import org.reflections.filters.Filter;
import org.reflections.util.DescriptorHelper;
import org.reflections.util.FluentIterable;
import org.reflections.util.Transformer;
import org.reflections.util.VirtualFile;
import static org.reflections.util.VirtualFile.urls2VirtualFiles;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class JavassistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo> {
    private final static Logger log = Logger.getLogger(JavassistAdapter.class);

    public List<FieldInfo> getFields(final ClassFile cls) {
        //noinspection unchecked
        return cls.getFields();
    }

    public List<MethodInfo> getMethods(final ClassFile cls) {
        //noinspection unchecked
        return cls.getMethods();
    }

    public String getMethodName(final MethodInfo method) {
        return method.getName();
    }

    public List<String> getParameterNames(final MethodInfo method) {
        return DescriptorHelper.methodDescriptorToParameterNameList(method.getDescriptor());
    }

    public List<String> getClassAnnotationNames(final ClassFile aClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) aClass.getAttribute(AnnotationsAttribute.visibleTag);
        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getFieldAnnotationNames(final FieldInfo field) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.visibleTag);

        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getMethodAnnotationNames(final MethodInfo method) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);

        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getParameterAnnotationNames(final MethodInfo method, final int parameterIndex) {
        ParameterAnnotationsAttribute parameterAnnotationsAttribute = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.visibleTag);

        if (parameterAnnotationsAttribute != null) {
            Annotation[][] annotations = parameterAnnotationsAttribute.getAnnotations();
            Annotation[] annotation = annotations[parameterIndex];
            return getAnnotationNames(annotation);
        } else {
            return new ArrayList<String>();
        }
    }

    public String getReturnTypeName(final MethodInfo method) {
        return DescriptorHelper.methodDescriptorToReturnTypeName(method.getDescriptor());
    }

    public String getFieldName(final FieldInfo field) {
        return field.getName();
    }

    public String getMethodKey(final MethodInfo method) {
        String descriptor = method.getDescriptor();
        String paramDescriptor = descriptor.substring(descriptor.indexOf("("), descriptor.lastIndexOf(")")+1);

        String methodKey = String.format("%s %s", getMethodName(method), paramDescriptor);
        return methodKey;
    }

    //

    public String getClassName(final ClassFile cls) {
        return cls.getName();
    }

    public String getSuperclassName(final ClassFile cls) {
        return cls.getSuperclass();
    }

    public List<String> getInterfacesNames(final ClassFile cls) {
        return Arrays.asList(cls.getInterfaces());
    }

    public Iterable<ClassFile> iterateClasses(final Collection<URL> urls) {
        return FluentIterable
                .iterate(urls)
                .fork(urls2VirtualFiles)
                .filter(classesOnly)
                .transform(virtualFile2ClassFile);
    }

    //
    private List<String> getAnnotationNames(final AnnotationsAttribute annotationsAttribute) {
        if (annotationsAttribute == null) {return new ArrayList<String>(0);}

        final Annotation[] annotations = annotationsAttribute.getAnnotations();
        return getAnnotationNames(annotations);
    }

    private List<String> getAnnotationNames(final Annotation[] annotations) {
        List<String> result = new ArrayList<String>();

        for (Annotation annotation : annotations) {
            result.add(annotation.getTypeName());
        }

        return result;
    }

    //
    //filter only VirtualFiles that are .class files
    private final static Filter<VirtualFile> classesOnly = new Filter<VirtualFile>() {
        public boolean accept(final VirtualFile virtualFile) {
            return virtualFile.getName().endsWith(".class");
        }
    };

    //transform VirtualFile to ClassFile
    private final static Transformer<VirtualFile, ClassFile> virtualFile2ClassFile = new Transformer<VirtualFile, ClassFile>() {
        public ClassFile transform(final VirtualFile virtualFile) {
            BufferedInputStream bis = null;
            try {
                return new ClassFile(
                        new DataInputStream(bis = new BufferedInputStream(virtualFile.getInputStream())));
            }
            catch (IOException e) {
                log.warn(String.format("ignoring IOException while scanning %s", virtualFile.getName()));
                return null;
            }
            finally {
                if (bis != null) {try {bis.close();} catch (IOException e) {throw new RuntimeException(e);}}
            }
        }
    };

}
