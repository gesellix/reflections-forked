package org.reflections.model.meta;

import java.util.*;

/**
 * @author mamo
 */
public class MetaClass extends MetaInterface {
    private Map<String,MetaClass> superClasses = new HashMap<String,MetaClass>();
    private Map<String,MetaField> fields = new HashMap<String,MetaField>();
    private List<MetaConstructor> constructors = new ArrayList<MetaConstructor>();

    public MetaClass(String type) {
        super(type);
    }

    public Collection<MetaClass> getSuperClasses() {return superClasses.values();}

    public Collection<MetaField> getFields() {return fields.values();}
    public MetaField getField(String name) {return fields.get(name);}

    public List<MetaConstructor> getConstructors() {return constructors;}
//    public MetaConstructor getConstructor(String name) {return constructors.get(name);}

    public MetaClass addSuperClasses(Collection<MetaClass> superClasses) {
        for (MetaClass superClass : superClasses) {
            this.superClasses.put(superClass.getType(), superClass);
        }
        return this;
    }

    public MetaClass addSuperClasses(MetaClass... superClasses) {
        for (MetaClass superClass : superClasses) {
            this.superClasses.put(superClass.getType(), superClass);
        }
        return this;
    }

    public MetaClass addFields(MetaField... fields) {
        for (MetaField field : fields) {
            this.fields.put(field.getName(),field);
        }
        return this;
    }

    public MetaClass addConstructors(Collection<MetaConstructor> constructors) {
        this.constructors.addAll(constructors);
        return this;
    }

    public MetaClass addConstructor(MetaConstructor constructor) {
        this.constructors.add(constructor);
        return this;
    }
}
