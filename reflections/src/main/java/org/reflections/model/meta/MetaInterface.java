package org.reflections.model.meta;

import org.reflections.model.meta.meta.FirstClassElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mamo
 */
public class MetaInterface extends FirstClassElement {
    private Map<String,MetaInterface> interfaces = new HashMap<String,MetaInterface>();

    public MetaInterface(String type) {
        super(type);
    }

    public Collection<MetaInterface> getInterfaces() {return interfaces.values();}

    public FirstClassElement addInterface(MetaInterface anInterface) {
        this.interfaces.put(anInterface.getType(),anInterface);
        return this;
    }

    public FirstClassElement addInterfaces(Collection<MetaInterface> interfaces) {
        for (MetaInterface anInterface : interfaces) {
            this.interfaces.put(anInterface.getType(),anInterface);
        }
        return this;
    }
}
