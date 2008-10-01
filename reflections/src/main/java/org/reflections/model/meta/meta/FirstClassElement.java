package org.reflections.model.meta.meta;

import org.reflections.model.meta.MetaMethod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
* @author mamo
*/
public abstract class FirstClassElement extends BasicElement {
    private Map<String,MetaMethod> methods = new HashMap<String,MetaMethod>();

    public FirstClassElement(String type) {super(type);}

    public Collection<MetaMethod> getMethods() {return methods.values();}
    public MetaMethod getMethod(String name) {return methods.get(name);}

    public FirstClassElement addMethods(MetaMethod... methods) {
        for (MetaMethod method : methods) {
            this.methods.put(method.getName(), method);
        }
        return this;
    }

    public FirstClassElement addMethod(MetaMethod method) {
        methods.put(method.getName(), method);
        return this;
    }
}
