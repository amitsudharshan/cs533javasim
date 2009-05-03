/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayDeque;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.BusClient;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.CacheControllerState.StateAnd;

/**
 *
 * @author amit
 */
public class CacheController<Msg extends AbstractBusMessage<Msg>> implements BusClient<Msg>, ComponentInterface, MemoryInterface {
    CacheControllerState state;
    ArrayDeque<MemoryInstruction> pendingRequests;
    MemoryInstruction incomingRequest;

    public void recieveMessage(Msg msg) {
        /*StateAnd<BusMessage> response = state.recieveBusMessage(msg);
        if (response == null) {
        return null;
        } else {
        state = response.nextState;
        return response.value;
        }*/
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Msg getResponse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Msg getBusMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BusAggregator<Msg> getAggregator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MemoryInstruction getMemoryRequest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        assert (incomingRequest == null);
        incomingRequest = instr;
    }

    public void runPrep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void runClock() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
