package org.reflections.helper;

import com.google.common.collect.Sets;
import org.reflections.model.meta.meta.TypedElement;

import java.util.Collection;

/**
 * @author mamo
 */
public abstract class TestHelper {

    //todo: move to somewhere else?
    public static Collection<String> toTypes(Collection<? extends TypedElement> typedElements) {
        Collection<String> result = Sets.newHashSet();
        for (TypedElement annotatedElement : typedElements) {
            result.add(annotatedElement.getType());
        }
        return result;
    }
}
