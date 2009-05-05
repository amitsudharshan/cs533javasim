/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayDeque;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.BusClient;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public abstract class CacheController<Msg extends AbstractBusMessage<Msg>>
        implements BusClient<Msg>, ComponentInterface, MemoryInterface {
    static Logger logger = Logger.getLogger(CacheController.class);
    
    protected CacheControllerState<Msg> state;
    protected ArrayDeque<MemoryInstruction> pendingRequests;
    protected MemoryInstruction incomingRequest;
    protected MemoryInstruction currentRequest;
    protected Msg busMessage;
    protected Msg responseToBus;
    protected boolean busResponseRead = false;
    protected Msg currentTransaction;
    protected MemoryInstruction recievedMemoryResponse;

    protected CacheController() {
        Simulator.registerComponent(this);
        pendingRequests = new ArrayDeque<MemoryInstruction>();
    }

    public void recieveMessage(Msg msg) {
        logger.debug("recieveMessage");
        assert (busMessage == null);
        busMessage = msg;
    }

    // probably the bus will only ask us once after
    // each thing that demands a response
    public Msg getResponse() {
        // logger.debug("getResponse");
        Msg resp = responseToBus;
        busResponseRead = true;
        return resp;
    }

    public Msg getBusMessage() {
        //logger.debug("getBusMessage");
        StateAnd<Msg, CacheControllerState<Msg>> start = state.startTransaction();
        if (start == null) {
            return null;
        }
        state = start.nextState;
        currentTransaction = start.value;
        return currentTransaction;
    }

    public BusAggregator<Msg> getAggregator() {
        logger.debug("getAggregator");
        if (currentTransaction == null) {
            return null;
        }
        return currentTransaction.getAggregator();
    }

    public MemoryInstruction getMemoryRequest() {
        logger.debug("getMemoryRequest");
        if (currentTransaction == null) {
            return null;
        }
        return currentTransaction.getMemoryRequest();
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        logger.debug("recieveMemoryResponse");
        assert (recievedMemoryResponse == null);
        recievedMemoryResponse = resp;
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        logger.debug("enqueueMemoryInstruction");
        assert (incomingRequest == null);
        incomingRequest = instr;
    }

    public void runPrep() {
    }

    public void runClock() {
        //logger.debug("runClock");
        if (busResponseRead) {
            responseToBus = null;
            busResponseRead = false;
        }

        if (incomingRequest != null) {
            logger.debug("got memory instruction");
            pendingRequests.addLast(incomingRequest);
            incomingRequest = null;
        }
        StateAnd<MemoryInstruction, CacheControllerState<Msg>> reply;
        if (currentRequest != null) {
            // logger.debug("checking if current memory request is completed");
            reply = state.pollRequestStatus(currentRequest);
        } else {
            currentRequest = pendingRequests.peekFirst();
            if (currentRequest != null) {
                logger.debug("pulled a memory instruction off the queue");
                reply = state.recieveClientRequest(currentRequest);
            } else {
                //logger.debug("no current or pending requests");
                reply = null;
            }
        }
        if (reply != null) {
            state = reply.nextState;
                MemoryInstruction resp = reply.value;
                if (resp != null) {
                    logger.debug("memory request finished");
                    if (resp != currentRequest) {
                        assert (resp.type == currentRequest.type);
                        assert (resp.inAddress == currentRequest.inAddress);
                        currentRequest.setOutData(resp.getOutData());
                    }
                    currentRequest.setIsCompleted(true);
                    currentRequest = null;
                } else {
                    logger.debug("transitioning to handle request");
                }
            }

        // FIXME tracking of currentTransaction - properly ending when aggregator and memory instruction null, etc.
        if (busMessage != null) {
            StateAnd<Msg, CacheControllerState<Msg>> response;
            if (currentTransaction != null) {
                logger.debug("recieved bus message - response");
                response = state.recieveBusResponse(busMessage);
                currentTransaction = response.value;
            } else {
                logger.debug("recieved bus message - other's broadcast");
                response = state.recieveBroadcastMessage(busMessage);
            }
            assert response != null;
            busMessage = null;
            if (response.value == null) {
                logger.debug("no response");
                responseToBus = null;
            } else {
                logger.debug("responded "+response.value.getTypeString());
                responseToBus = response.value;
            }
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