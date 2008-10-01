package org.reflections.model.meta.meta;

import org.reflections.model.meta.MetaAnnotation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mamo
 */
public abstract class BasicElement implements TypedElement, AnnotatedElement, GenericDeclaration {
    private String type;
    private Map<String,MetaAnnotation> annotations = new HashMap<String,MetaAnnotation>();

    public BasicElement() {}
    public BasicElement(String type) {setType(type);}

    public String getType() {return type;}

    public Collection<MetaAnnotation> getAnnotations() {return annotations.values();}
    public MetaAnnotation getAnnotation(String annotation) {return annotations.get(annotation);}

    public BasicElement setType(String type) {
        this.type = type;
        return this;
    }

    public BasicElement addAnnotation(MetaAnnotation annotation) {
        this.annotations.put(annotation.getType(),annotation);
        return this;
    }

    public BasicElement addAnnotations(Collection<MetaAnnotation> annotations) {
        for (MetaAnnotation annotation : annotations) {
            this.annotations.put(annotation.getType(),annotation);
        }
        return this;
    }

    public BasicElement addAnnotations(MetaAnnotation... annotations) {
        for (MetaAnnotation annotation : annotations) {
            this.annotations.put(annotation.getType(),annotation);
        }
        return this;
    }

    public String toString() {return type;}
}
