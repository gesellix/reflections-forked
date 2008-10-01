package org.reflections.model;

/**
 * @author mamo
 */
public enum ElementTypes {
    supertypes,
    methods,
    fields,
    annotations;

    public static ElementTypes[] all() {
        return values();
    }
}
