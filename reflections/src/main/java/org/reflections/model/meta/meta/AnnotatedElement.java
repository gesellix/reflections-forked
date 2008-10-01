package org.reflections.model.meta.meta;

import org.reflections.model.meta.MetaAnnotation;

import java.util.Collection;

/**
 * @author mamo
 */
public interface AnnotatedElement {
    Collection<MetaAnnotation> getAnnotations();}
