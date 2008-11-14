package org.reflections.adapters;

import com.google.common.collect.AbstractIterator;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import org.reflections.filters.Filter;
import org.reflections.util.DescriptorHelper;
import org.reflections.util.EmptyIterator;
import org.reflections.util.VirtualFile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class JavassistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo> {
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

    public Iterator<ClassFile> iterateClasses(final Collection<URL> urls, final Filter<String> filter) {
        return new AbstractIterator<ClassFile>() {
            private Iterator<URL> urlsIterator = urls.iterator();
            private Iterator<VirtualFile> virtualFileIterator = new EmptyIterator<VirtualFile>();

            @SuppressWarnings({"ThrowFromFinallyBlock"})
            protected ClassFile computeNext() {
                while (true) {
                    if (!virtualFileIterator.hasNext()) {
                        if (!urlsIterator.hasNext()) {
                            return endOfData();
                        } else {
                            URL url = urlsIterator.next();
                            virtualFileIterator = VirtualFile.iterable(url).iterator();
                        }
                    } else {
                        VirtualFile virtualFile = virtualFileIterator.next();
                        InputStream inputStream = virtualFile.getInputStream();
                        BufferedInputStream bis = null;
                        try {
                            bis = new BufferedInputStream(inputStream);
                            DataInputStream dis = new DataInputStream(bis);
                            ClassFile classFile = new ClassFile(dis);
                            String className = getClassName(classFile);
                            if (filter.accept(className)) {
                                return classFile;
                            }
                        }
                        catch (IOException e) {
                            System.out.println(e.getMessage());
//                            throw new RuntimeException(e);
                        }
                        finally {
                            if (bis!=null) {
                                try {bis.close();} catch (IOException e) {throw new RuntimeException(e);}
                            }
                        }
                    }
                }
            }
        };
    }

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
}
