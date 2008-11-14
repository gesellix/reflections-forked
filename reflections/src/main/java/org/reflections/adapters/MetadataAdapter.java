package org.reflections.adapters;

import org.reflections.filters.Filter;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public interface MetadataAdapter<C,F,M> {

    //
    String getClassName(final C cls);

    String getSuperclassName(final C cls);

    List<String> getInterfacesNames(final C cls);

    Iterator<C> iterateClasses(final Collection<URL> urls, final Filter<String> filters);

    //
    List<F> getFields(final C cls);

    List<M> getMethods(final C cls);

    String getMethodName(final M method);

    List<String> getParameterNames(final M method);

    List<String> getClassAnnotationNames(final C aClass);

    List<String> getFieldAnnotationNames(final F field);

    List<String> getMethodAnnotationNames(final M method);

    List<String> getParameterAnnotationNames(final M method, final int parameterIndex);

    String getReturnTypeName(final M method);

    String getFieldName(final F field);

    String getMethodKey(final M method);
}
