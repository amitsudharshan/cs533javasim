/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayDeque;
import java.util.concurrent.LinkedBlockingDeque;
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

    protected Logger logger;
    private CacheControllerState<Msg> state;
    //protected ArrayDeque<MemoryInstruction> pendingRequests;
    protected LinkedBlockingDeque<MemoryInstruction> pendingRequests;
    protected MemoryInstruction currentRequest;
    protected Msg responseToBus;
    protected Msg currentTransaction;
    protected MemoryInstruction recievedMemoryResponse;
    private boolean waitingForBusReply = false;

    public void setState(CacheControllerState<Msg> state) {
        this.state = state;
        logger.debug("state -> " + state.toString());
    }

    public CacheControllerState<Msg> getState() {
        return state;
    }

    public Logger getChildLogger(String childName) {
        return Logger.getLogger(logger.getName() + "." + childName);
    }

    protected CacheController(Logger logger) {
        this.logger = logger;
        Simulator.registerComponent(this);
        pendingRequests = new LinkedBlockingDeque<MemoryInstruction>();
    }

    public void recieveMessage(Msg msg) {
        logger.debug("recieveMessage(" + msg.toString() + ")");
        if (currentTransaction != null) {
            StateAnd<Msg, CacheControllerState<Msg>> response =
                    state.receiveBusResponse(msg);
            // need to set up next phase right now
            logger.debug(response);
            assert response != null;
            if (response.value == null) {
                assert response.value != null;
            }
            currentTransaction = response.value;
            setState(response.nextState);
        } else {
            if (waitingForBusReply) {
                waitingForBusReply = false;
                // The bus master can arrange for multiple rounds of
                // communication if it likes
                StateAnd<Msg, CacheControllerState<Msg>> response =
                        state.receiveBusResponse(msg);
                if (response != null) {
                    logger.debug("recieveBusResponse => " + response.toString());
                    setState(response.nextState);
                } else {
                    logger.debug("recieveBusResponse => null");
                }
            } else {
                // TODO - will not work with hierarchial caches,
                // this assumes that our states can always reply to
                // a message-requiring-response immediately.
                StateAnd<Msg, CacheControllerState<Msg>> response =
                        state.recieveBroadcastMessage(msg);
                assert response != null;
                logger.debug("recieveBroadcastMessage => " + response.toString());
                setState(response.nextState);
                responseToBus = response.value;
            }
        }
    }

    // probably the bus will only ask us once after
    // each thing that demands a response
    public Msg getResponse() {
        // state was responsible for setting a reply
        // right away in runClock if one was needed,
        // so we must have a message
        assert responseToBus != null;
        logger.debug("getResponse => " + responseToBus.toString());
        // bus will not call getResponse again after
        // getting a response without sending us another
        // message in between, so null out the field.
        Msg m = responseToBus;
        responseToBus = null;
        waitingForBusReply = true;
        return m;
    }

    public Msg getBusMessage() {
        if (currentTransaction != null) {
            logger.debug("getBusMessage => " + currentTransaction.toString());
            return currentTransaction;
        } else {
            logger.debug("getBusMessage");
            StateAnd<Msg, CacheControllerState<Msg>> start = state.startTransaction();
            if (start == null) {
                logger.debug("getBusMessage => null");
                return null;
            }
            logger.debug("getBusMessage => " + start.toString());
            setState(start.nextState);
            currentTransaction = start.value;
            return currentTransaction;
        }
    }

    public BusAggregator<Msg> getAggregator() {
        logger.debug("getAggregator");
        assert currentTransaction != null;
        return currentTransaction.getAggregator();
    }

    public MemoryInstruction getMemoryRequest() {
        logger.debug("getMemoryRequest");
        assert currentTransaction != null;
        return currentTransaction.getMemoryRequest();
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        logger.debug("recieveMemoryResponse(" + resp.toString() + ")");
        if (currentTransaction != null) {
            StateAnd<Msg, CacheControllerState<Msg>> response =
                    state.recieveMemoryResponse(resp);
            logger.debug("recieveMemoryResponse => " + response.toString());
            assert response != null;
            assert response.value != null;
            currentTransaction = response.value;
            setState(response.nextState);
        } else {
            // a client
            assert (recievedMemoryResponse == null);
            recievedMemoryResponse = resp;
        }
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        logger.debug("enqueueMemoryInstruction(" + instr.toString() + ")");
        pendingRequests.addLast(instr);
    }

    public void runPrep() {
    }

    public void runClock() {
        logger.debug("runClock");
        if (currentRequest != null) {
            logger.debug("checking if current memory request is completed");
            StateAnd<MemoryInstruction, CacheControllerState<Msg>> response = state.pollRequestStatus(currentRequest);
            if (response == null) {
                logger.debug("pollRequestStatus => null");
            } else {
                logger.debug("pollRequestStatus => " + response.toString());
                setState(response.nextState);
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
                StateAnd<MemoryInstruction, CacheControllerState<Msg>> response = state.recieveClientRequest(nextRequest);
                if (response != null) {

                    // state accepted the client request
                    currentRequest = nextRequest;
                    // actually remove the request
                    pendingRequests.pollFirst();
                    setState(response.nextState);
                    MemoryInstruction reply = response.value;
                    if (reply != null) {
                        Simulator.logEvent("CacheController: Got Cache Hit");
                        Simulator.logEvent("CacheController: Cache hit for type: " + nextRequest.getType() + " for address 0x" + Integer.toHexString(nextRequest.getInAddress()));
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
                    } else {
                        Simulator.logEvent("CacheController: Got Cache Miss");
                        Simulator.logEvent("CacheController: Cache Miss for type: " + nextRequest.getType() + " for address 0x" + Integer.toHexString(nextRequest.getInAddress()));

                    }
                // otherwise we will have to keep polling state as above.
                }
            } else {
                //logger.debug("no current or pending requests");
            }
        }

        if (recievedMemoryResponse != null) {
            assert currentTransaction == null;
            StateAnd<Msg, CacheControllerState<Msg>> response = state.snoopMemoryResponse(recievedMemoryResponse);
            assert response != null;
            assert response.value == null;
            setState(response.nextState);
            recievedMemoryResponse = null;
        }
        if (currentTransaction != null && currentTransaction.getAggregator() == null && currentTransaction.getMemoryRequest() == null) {
            // finished transaction
            logger.debug("Finished Transaction");
            currentTransaction = null;
        }
    }

    public int getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}