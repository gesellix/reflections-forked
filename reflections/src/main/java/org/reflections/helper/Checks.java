package org.reflections.helper;

/**
 * @author mamo
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Checks {
    public static void checkNotNull(Object object) {
        if (object==null) {
            throw new RuntimeException(String.format("Object %s can not be null", object));
        }
    }
}
