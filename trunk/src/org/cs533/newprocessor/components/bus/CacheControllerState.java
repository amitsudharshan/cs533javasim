/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.bus;

import org.apache.log4j.Logger;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;

/**
 *
 * @author amit
 */
public abstract class CacheControllerState<Msg extends AbstractBusMessage<Msg>> {
    protected Logger logger;

    protected final <V> StateAnd<V, CacheControllerState<Msg>> noJump(V v) {
        return new StateAnd(v, this);
    }

    protected final <V> StateAnd<V, CacheControllerState<Msg>> andJump(V v, CacheControllerState next) {
        return new StateAnd(v, next);
    }

    protected final <V> StateAnd<V, CacheControllerState<Msg>> jumpTo(CacheControllerState next) {
        return new StateAnd(null, next);
    }

    protected final <V> StateAnd<V, CacheControllerState<Msg>> noReply() {
        return new StateAnd(null, this);
    }

    public StateAnd<Msg, CacheControllerState<Msg>> recieveBroadcastMessage(Msg b) {
        logger.debug("recieveBroadcastMessage("+b.toString()+")");
        return null;
    }

    public StateAnd<Msg, CacheControllerState<Msg>> receiveBusResponse(Msg b) {
        logger.debug("recieveBusResponse("+b.toString()+")");
        return null;
    }

    public StateAnd<Msg, CacheControllerState<Msg>> snoopMemoryResponse(MemoryInstruction response) {
        logger.debug("snoopMemoryResponse");
        return null;
    }

    public StateAnd<Msg, CacheControllerState<Msg>> recieveMemoryResponse(MemoryInstruction response) {
        logger.debug("recieveMemoryResponse");
        return null;
    }

    public StateAnd<MemoryInstruction, CacheControllerState<Msg>> recieveClientRequest(MemoryInstruction request) {
        logger.debug("recieveClientRequest");
        return null;
    }

    public StateAnd<MemoryInstruction, CacheControllerState<Msg>> pollRequestStatus(MemoryInstruction request) {
        logger.debug("pollRequestStatus");
        return null;
    }

    public StateAnd<Msg, CacheControllerState<Msg>> startTransaction() {
        logger.debug("startTransaction");
        return null;
    }
}