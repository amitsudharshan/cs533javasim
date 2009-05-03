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
import org.cs533.newprocessor.components.bus.StateAnd;

/**
 *
 * @author amit
 */
public class CacheController<Msg extends AbstractBusMessage<Msg>>
        implements BusClient<Msg>, ComponentInterface, MemoryInterface {

    CacheControllerState<Msg> state;
    ArrayDeque<MemoryInstruction> pendingRequests;
    MemoryInstruction incomingRequest;
    boolean currentRequestAccepted = false;
    MemoryInstruction currentRequest;
    Msg busRequest;
    Msg responseToBus;
    boolean busResponseRead = false;
    Msg currentTransaction;
    MemoryInstruction recievedMemoryResponse;

    public void recieveMessage(Msg msg) {
        assert (busRequest == null);
        busRequest = msg;
    }

    // probably the bus will only ask us once after
    // each thing that demands a response
    public Msg getResponse() {
        Msg resp = responseToBus;
        busResponseRead = true;
        return resp;
    }

    public Msg getBusMessage() {
        StateAnd<Msg, CacheControllerState<Msg>> start = state.startTransaction();
        if (start == null) {
            return null;
        }
        state = start.nextState;
        currentTransaction = start.value;
        return currentTransaction;
    }

    public BusAggregator<Msg> getAggregator() {
        if (currentTransaction == null) {
            return null;
        }
        return currentTransaction.getAggregator();
    }

    public MemoryInstruction getMemoryRequest() {
        if (currentTransaction == null) {
            return null;
        }
        return currentTransaction.getMemoryRequest();
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        assert (recievedMemoryResponse == null);
        recievedMemoryResponse = resp;
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        assert (incomingRequest == null);
        incomingRequest = instr;
    }

    public void runPrep() {
    }

    public void runClock() {
        if (busResponseRead) {
            responseToBus = null;
            busResponseRead = false;
        }

        if (incomingRequest != null) {
            pendingRequests.addLast(incomingRequest);
            incomingRequest = null;
        }
        if (currentRequest == null) {
            currentRequest = pendingRequests.pollFirst();
        }
        if (currentRequest != null) {
            StateAnd<MemoryInstruction, CacheControllerState<Msg>> reply =
                    state.recieveClientRequest(incomingRequest);
            if (reply != null) {
                currentRequestAccepted = true;
                state = reply.nextState;
                MemoryInstruction resp = reply.value;
                if (resp != null) {
                    if (resp != currentRequest) {
                        assert (resp.type == currentRequest.type);
                        assert (resp.inAddress == currentRequest.inAddress);
                        currentRequest.setOutData(resp.getOutData());
                    }
                    currentRequest.setIsCompleted(true);
                    currentRequest = null;
                    currentRequestAccepted = false;
                }
            }
        }

        StateAnd<Msg, CacheControllerState<Msg>> response = state.recieveBusMessage(busRequest);
        assert (response != null);
        if (currentTransaction != null) {
            currentTransaction = response.value;
        } else {
            responseToBus = response.value;
        }
        state = response.nextState;
        busRequest = null;

        if (recievedMemoryResponse != null) {
            if (currentTransaction != null) {
                assert (recievedMemoryResponse == currentTransaction.getMemoryRequest());
                StateAnd<Msg, CacheControllerState<Msg>> nextRound = state.recieveMemoryResponse(recievedMemoryResponse);
                currentTransaction = nextRound.value;
                state = nextRound.nextState;
            } else {
                StateAnd<Msg, CacheControllerState<Msg>> x = state.snoopMemoryResponse(recievedMemoryResponse);
                assert (x != null);
                assert (x.value == null);
                state = x.nextState;
            }
            recievedMemoryResponse = null;
        }
    }

    public int getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}