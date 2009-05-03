package org.cs533.newprocessor.components.bus;

public class StateAnd<V,S> {

    public final V value;
    public final S nextState;

    StateAnd(V value, S next) {
        super();
        this.value = value;
        this.nextState = next;
    }
}
