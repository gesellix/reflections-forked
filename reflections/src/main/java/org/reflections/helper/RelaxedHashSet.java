package org.reflections.helper;

import java.util.HashSet;
import java.util.Collection;

/**
 * @author mamo
*/
public class RelaxedHashSet<E> extends HashSet<E> {
    public static <E> RelaxedHashSet<E> create() {
        return new RelaxedHashSet<E>();
    }
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (collection==null) {return false;} //relax, don't do it
        return super.addAll(collection);
    }
}
