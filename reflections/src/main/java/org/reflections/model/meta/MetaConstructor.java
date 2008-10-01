package org.reflections.model.meta;

import org.reflections.model.meta.meta.Member;
import org.reflections.model.meta.meta.FirstClassElement;

import java.util.ArrayList;
import java.util.List;

/**
* @author mamo
*/
public class MetaConstructor extends Member {
    private List<MetaField> parameters = new ArrayList<MetaField>();

    public MetaConstructor(FirstClassElement owner) {super(owner);}

    public List<MetaField> getParameters() {return parameters;}
    public MetaField getParameter(int index) {return parameters.get(index);}

    public void setParameters(List<MetaField> parameters) {this.parameters = parameters;}


}
