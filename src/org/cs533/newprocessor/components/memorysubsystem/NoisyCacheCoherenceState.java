/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.CacheControllerState;

import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.simulator.Simulator;
import java.util.Arrays;

/**
 *
 * @author brandon
 */
public class NoisyCacheCoherenceState<Msg extends AbstractBusMessage<Msg>> extends CacheControllerState<Msg> {
    CacheControllerState<Msg> wrappedState;

    @Override
    public String toString() {
        return "NoisyCacheCoherenceState("+wrappedState.toString()+")";
    }

    public NoisyCacheCoherenceState(CacheControllerState<Msg> wrappedState) {
        this.wrappedState = wrappedState;
        logger = wrappedState.getLogger();
    }

    protected void preCall(String m, Object... args) {
        logger.debug(Simulator.getPhase()+":"+m+" ("+Arrays.toString(args)+")");
    }
    protected <V> StateAnd<V,CacheControllerState<Msg>> logCall(String m, StateAnd<V,CacheControllerState<Msg>> result, Object... args) {
        String resultString;
        if (result == null ) {
            resultString = "null";
        } else {
            resultString = result.toString();
        }
        logger.debug(Simulator.getPhase()+":"+m+" ("+Arrays.toString(args)+") -> "+resultString);
        if (result != null) {
            return new StateAnd(result.value, new NoisyCacheCoherenceState(result.nextState));
        } else {
            return null;
        }
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<Msg>> pollRequestStatus(MemoryInstruction request) {
        preCall("pollRequestStatus", request);
        return logCall("pollRequestStatus", wrappedState.pollRequestStatus(request), request);
    }

    @Override
    public StateAnd<Msg, CacheControllerState<Msg>> receiveBusResponse(Msg b) {
        preCall("receiveBusReponse", b);
        return logCall("receiveBusReponse", wrappedState.receiveBusResponse(b), b);
    }

    @Override
    public StateAnd<Msg, CacheControllerState<Msg>> recieveBroadcastMessage(Msg b) {
        preCall("recieveBroadcastMessage", b);
        return logCall("recieveBroadcastMessage", wrappedState.recieveBroadcastMessage(b), b);
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<Msg>> recieveClientRequest(MemoryInstruction request) {
        preCall("recieveClientRequest", request);
        return logCall("recieveClientRequest",wrappedState.recieveClientRequest(request), request);
    }

    @Override
    public StateAnd<Msg, CacheControllerState<Msg>> recieveMemoryResponse(MemoryInstruction response) {
        preCall("recieveMemoryResponse",response);
        return logCall("recieveMemoryResponse",wrappedState.recieveMemoryResponse(response), response);
    }

    @Override
    public StateAnd<Msg, CacheControllerState<Msg>> snoopMemoryResponse(MemoryInstruction response) {
        preCall("snoopMemoryResponse", response);
        return logCall("snoopMemoryResponse",wrappedState.snoopMemoryResponse(response), response);
    }

    @Override
    public StateAnd<Msg, CacheControllerState<Msg>> startTransaction() {
        preCall("startTransaction");
        return logCall("startTransaction", wrappedState.startTransaction());
    }
}