package org.reflections.actors.impl;

import org.reflections.helper.DescriptorHelper;
import org.reflections.model.ClasspathMD;
import org.reflections.model.meta.MetaField;
import org.reflections.model.meta.MetaMethod;
import org.reflections.model.meta.meta.BasicElement;
import org.reflections.model.meta.meta.FirstClassElement;
import org.reflections.model.meta.meta.Member;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Reflections query DSL api, allowing queries such as
 * todo
 * @author mamo
*/

//some query candidates:
//get{class|method|field}annotatedWith[annotation values]
//getMethodsWithParams(params...)
//getImplementingClasses
//getImplementingClasses(superType).annotatedWith(annotatonType|annotationInstance)
//getMethods(method matching annotation instance - which can be used to annotate a method as well)
//getMethods(aspectj like dsl) //"execution(* product.ProductService.*(..))"


public class QueryDSL {
    private final ClasspathMD classpathMD;

    public QueryDSL(final ClasspathMD classpathMD) {
        this.classpathMD = classpathMD;
    }

    public <T extends BasicElement> S1<T> select(Class<T> elementType) {
        return new S1<T>(elementType);
    }

    public class S1<T extends BasicElement> {
        private Set<T> result;
        private final Class<T> resultElementsType;

        private S1(Class<T> resultElementsType) {this.resultElementsType = resultElementsType;}

        public S1<T> annotatedWith(Class<? extends Annotation> annotation) {
            final Set<BasicElement> annotatedWith = classpathMD.getReverseFirstClassElements(annotation.getName());
            retainResults(annotatedWith);
            return this;
        }

        public S1<T> annotatedWith(Annotation annotation) {
            Collection<BasicElement> subResult = new HashSet<BasicElement>();

            final Class<? extends Annotation> annotationType = annotation.annotationType();
            final Map<String, Object> requestedAnnotationMap = getAnnotationMap(annotation);

            for (BasicElement candidateElement : classpathMD.getReverseFirstClassElements(annotationType.getName())) {
                final Annotation candidateAnnotation = getJavaAnnotatedElement(candidateElement).getAnnotation(annotationType);

                if (requestedAnnotationMap.equals(getAnnotationMap(candidateAnnotation))) {
                    subResult.add(candidateElement);
                }
            }

            retainResults(subResult);
            return this;
        }

        public S1<T> ofType(Class<?> type) {
            final Set<BasicElement> set = classpathMD.getReverseFirstClassElements(DescriptorHelper.className(type));
            retainResults(set);
            return this;
        }

        public Set<T> query() {
            if (result==null) {
                //incase query was not filtered, return all of resultElementsType
                final Collection<FirstClassElement> all = classpathMD.getTypes();
                retainResults(all);
            }

            return result;
        }

        private void retainResults(Collection<? extends BasicElement> subset) {
            if (result==null) {
                result = new HashSet<T>();
                for (BasicElement element : subset) {
                    if (element.getClass() == resultElementsType) {
                        //noinspection unchecked
                        result.add((T) element);
                    }
                }
            } else {
                result.retainAll(subset);
            }
        }

    }

    //
    /**
     * returns a map where keys are annotation's method name and value is the returned value from that method
     */
    private Map<String/*parameter name*/, Object/*value*/> getAnnotationMap(Annotation annotation) {
        final Method[] methods = annotation.annotationType().getDeclaredMethods();
        Map<String, Object> parameters = new HashMap<String, Object>(methods.length);
        for (final Method method : methods) {
            try {parameters.put(method.getName(), method.invoke(annotation));}
            catch (Exception e) {throw new RuntimeException(e);} //todo: better log
        }

        return parameters;
    }

    private AnnotatedElement getJavaAnnotatedElement(BasicElement candidateElement) {
        AnnotatedElement javaAnnotatedElement;

        try {
            if (candidateElement instanceof FirstClassElement) {
                javaAnnotatedElement = Class.forName(candidateElement.getType());

            } else if (candidateElement instanceof Member) {
                final FirstClassElement owner = ((Member) candidateElement).getOwner();
                final Class<?> ownerClass = Class.forName(owner.getType());

                if (candidateElement instanceof MetaMethod) {
                    //todo: get the actual candidateElement that is originly (not trasnsitively) annotated with that annotation, so we can question it's parameters
                    final MetaMethod candidateMethod = (MetaMethod) candidateElement;
                    final Class<?>[] parameterTypes = getParameterTypes(candidateMethod);
                    javaAnnotatedElement = ownerClass.getDeclaredMethod(candidateMethod.getName(), parameterTypes);

                } else if (candidateElement instanceof MetaField) {
                    final MetaField candidateField = (MetaField) candidateElement;
                    javaAnnotatedElement = ownerClass.getDeclaredField(candidateField.getName());

                } else {throw new UnsupportedOperationException();} //should not get here
            } else {throw new UnsupportedOperationException();}  //should not get here

        } catch (Exception e) {
            throw new RuntimeException(e); //todo: better log
        }

        return javaAnnotatedElement;
    }

    private Class<?>[] getParameterTypes(MetaMethod candidateMethod) throws ClassNotFoundException {//get parameterTypes
        final List<MetaField> parameters = candidateMethod.getParameters();
        final Class<?>[] parameterTypes = new Class<?>[parameters.size()];
        for (int i=0;i<parameters.size();i++) {
            parameterTypes[i] = Class.forName(parameters.get(i).getType());
        }
        return parameterTypes;
    }
}
