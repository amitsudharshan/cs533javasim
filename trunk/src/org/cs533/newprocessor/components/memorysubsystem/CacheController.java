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
public abstract class CacheController<Msg extends AbstractBusMessage<Msg>>
        implements BusClient<Msg>, ComponentInterface, MemoryInterface {

    protected CacheControllerState<Msg> state;
    protected ArrayDeque<MemoryInstruction> pendingRequests;
    protected MemoryInstruction incomingRequest;
    protected MemoryInstruction currentRequest;
    protected Msg busMessage;
    protected Msg responseToBus;
    protected boolean busResponseRead = false;
    protected Msg currentTransaction;
    protected MemoryInstruction recievedMemoryResponse;

    public void recieveMessage(Msg msg) {
        assert (busMessage == null);
        busMessage = msg;
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
        StateAnd<MemoryInstruction, CacheControllerState<Msg>> reply;
        if (currentRequest != null) {
            reply = state.pollRequestStatus(currentRequest);
        } else {
            currentRequest = pendingRequests.peekFirst();
            if (currentRequest != null) {
                reply = state.recieveClientRequest(currentRequest);
            } else {
                reply = null;
            }
        }
        if (reply != null) {
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
                }
            }

        // FIXME tracking of currentTransaction - properly ending when aggregator and memory instruction null, etc.
        if (busMessage != null) {
            StateAnd<Msg, CacheControllerState<Msg>> response;
            if (currentTransaction != null) {
                response = state.recieveBusResponse(busMessage);
                currentTransaction = response.value;
            } else {
                response = state.recieveBroadcastMessage(busMessage);
            }
            responseToBus = response.value;
            state = response.nextState;
        }

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