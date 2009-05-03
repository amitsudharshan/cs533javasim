/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
/**
 *
 * @author amit
 */
public abstract class CacheControllerState<Msg extends AbstractBusMessage<Msg>> {
    public class StateAnd<V> {
        public final V value;
        public final CacheControllerState nextState;
        StateAnd(V value, CacheControllerState next) {
            this.value = value;
            this.nextState = next;
        }
    }
    protected <V> StateAnd<V> same(V v) { return new StateAnd(v,this); }
    protected <V> StateAnd<V> andJump(V v, CacheControllerState next) {
        return new StateAnd(v, next);
    }

    public StateAnd<Msg> recieveBusMessage(Msg b) {
        return null;
    }
    public StateAnd<Msg> snoopMemoryResponse(MemoryInstruction response) {
        return null;
    }
    public StateAnd<MemoryInstruction> recieveClientRequest(MemoryInstruction request) {
        return null;
    }
    public StateAnd<Msg> startTransaction() {
        return null;
    }
}