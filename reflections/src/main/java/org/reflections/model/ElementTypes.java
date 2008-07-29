package org.reflections.model;

/**
 * @author mamo
 */
public enum ElementTypes {
    supertypes,interfaces,methods,fields,
    annotations,
    parameterAnnotations,fieldAnnotations,methodAnnotations;

    public static ElementTypes[] all() {
        return values();
    }
}
