package org.reflections.helper;

import com.google.common.collect.Sets;
import org.junit.Assert;

import java.util.Collection;
import java.util.Set;

/**
 * @author mamo
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Asserts {

    public static <T> void collectionsContained(Collection<T> c1, Collection<T> c2) {
        boolean areContained = c1.containsAll(c2) && c2.containsAll(c1);
        Assert.assertTrue(areContained);
    }

    public static <T> void collectionsContained(Iterable<T> i1, Iterable<T> i2) {
        Set<T> set1 = Sets.newHashSet(i1);
        Set<T> set2 = Sets.newHashSet(i2);
        boolean areContained = set1.containsAll(set2) && set2.containsAll(set1);
        Assert.assertTrue(areContained);
    }
}
