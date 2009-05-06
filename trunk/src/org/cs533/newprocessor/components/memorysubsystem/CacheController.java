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
    protected MemoryInstruction currentRequest;
    protected Msg requestFromBus;
    protected Msg responseToBus;
    protected boolean busResponseRead = false;
    protected Msg currentTransaction;
    protected MemoryInstruction recievedMemoryResponse;
    private boolean waitingForBusReply = false;

    protected CacheController() {
        Simulator.registerComponent(this);
        pendingRequests = new ArrayDeque<MemoryInstruction>();
    }

    public void recieveMessage(Msg msg) {
        logger.debug("recieveMessage");
        if (currentTransaction != null) {
            StateAnd<Msg, CacheControllerState<Msg>> response =
                    state.recieveBusResponse(msg);
            // need to set up next phase right now
            assert response != null;
            assert response.value != null;
            currentTransaction = response.value;
            state = response.nextState;
        } else {
            assert requestFromBus == null;
            assert responseToBus == null;
            requestFromBus = msg;
        }
    }

    // probably the bus will only ask us once after
    // each thing that demands a response
    public Msg getResponse() {
        logger.debug("getResponse");
        // state was responsible for setting a reply
        // right away in runClock if one was needed
        assert responseToBus != null;
        // bus will not call getResponse again after
        // getting a response without sending us another
        // message in between.
        assert !busResponseRead;
        waitingForBusReply = true;
        busResponseRead = true;
        return responseToBus;
    }

    public Msg getBusMessage() {
        logger.debug("getBusMessage");
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
        if (currentTransaction != null) {
            StateAnd<Msg,CacheControllerState<Msg>> response =
                state.recieveMemoryResponse(resp);
            assert response != null;
            assert response.value != null;
            currentTransaction = response.value;
            state = response.nextState;
        } else {
            // a client
            assert (recievedMemoryResponse == null);
            recievedMemoryResponse = resp;
        }
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        logger.debug("enqueueMemoryInstruction");
        pendingRequests.addLast(instr);
    }

    public void runPrep() {
    }

    public void runClock() {
        // logger.debug("runClock");
        if (busResponseRead) {
            responseToBus = null;
            busResponseRead = false;
        }
        if (currentRequest != null) {
            // logger.debug("checking if current memory request is completed");
            StateAnd<MemoryInstruction, CacheControllerState<Msg>> response
                = state.pollRequestStatus(currentRequest);
            if (response != null) {
                state = response.nextState;
                MemoryInstruction reply = response.value;
                if (reply != null) {
                    // actuall done with the memory request.
                    assert reply.type == currentRequest.type;
                    assert reply.getInAddress() == currentRequest.getInAddress();
                    if (reply != currentRequest) {
                        currentRequest.setOutData(reply.getOutData());
                    }
                    // set is completed, client will notice by polling
                    currentRequest.setIsCompleted(true);
                    currentRequest = null;
                }
            }
        } else {
            MemoryInstruction nextRequest = pendingRequests.peekFirst();
            if (nextRequest != null) {
                logger.debug("pulled a memory instruction off the queue");
                StateAnd<MemoryInstruction, CacheControllerState<Msg>> response
                    = state.recieveClientRequest(nextRequest);
                if (response != null) {
                    // state accepted the client request
                    currentRequest = nextRequest;
                    MemoryInstruction reply = response.value;
                    if (reply != null) {
                        //state handled right away
                        // actuall done with the memory request.
                        assert reply.type == currentRequest.type;
                        assert reply.getInAddress() == currentRequest.getInAddress();
                        if (reply != currentRequest) {
                            currentRequest.setOutData(reply.getOutData());
                        }
                        // set is completed, client will notice by polling
                        currentRequest.setIsCompleted(true);
                        currentRequest = null;
                    }
                    // otherwise we will have to keep polling state as above.
                }
            } else {
                //logger.debug("no current or pending requests");
            }
        }
    
        if (requestFromBus != null) {
            assert currentTransaction == null;
            if (waitingForBusReply) {
                // The bus master can arrange for multiple rounds of
                // communication if it likes
                StateAnd<Msg, CacheControllerState<Msg>> response =
                        state.recieveBusResponse(requestFromBus);
                assert response != null;
                responseToBus = response.value;
                busResponseRead = false;
                waitingForBusReply = false;
                state = response.nextState;
            } else {
                // TODO - will not work with hierarchial caches,
                // this assumes that our states can always reply to
                // a message-requiring-response immediately.
                StateAnd<Msg, CacheControllerState<Msg>> response =
                        state.recieveBroadcastMessage(requestFromBus);
                assert response != null;
                state = response.nextState;                        
                responseToBus = response.value;
                busResponseRead = false;
            }
        }

        if (recievedMemoryResponse != null) {
            assert currentTransaction == null;
            StateAnd<Msg, CacheControllerState<Msg>> response = state.snoopMemoryResponse(recievedMemoryResponse);
            assert response != null;
            assert response.value == null;
            state = response.nextState;
            recievedMemoryResponse = null;
        }
        if (currentTransaction != null
                && currentTransaction.getAggregator() == null
                && currentTransaction.getMemoryRequest() == null) {
            // finished transaction
            logger.debug("Finished Transaction");
            currentTransaction = null;
        }
    }

    public int getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}