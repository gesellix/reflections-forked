package org.reflections.model.meta.meta;

/**
* @author mamo
*/
public abstract class Member extends BasicElement {
    private String name;
    private final FirstClassElement owner;

    public Member(FirstClassElement owner) {this.owner=owner;}

    public FirstClassElement getOwner() {return owner;}

    public String getName() {return name;}
    public Member setName(String name) {this.name=name; return this;}

    public String toString() {return getType()+" "+ name;}
}
