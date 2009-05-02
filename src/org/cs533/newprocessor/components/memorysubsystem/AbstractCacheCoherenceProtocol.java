/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.concurrent.BrokenBarrierException;
import org.cs533.newprocessor.AsyncComponentInterface;
import org.cs533.newprocessor.components.bus.BusAggregator;
import java.util.concurrent.CyclicBarrier;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;
/**
 *
 * @author Vivek
 */
public abstract class AbstractCacheCoherenceProtocol<BusMessage, LineState> implements
        AsyncComponentInterface, CoherenceProtocol<BusMessage, LineState>
{
    abstract protected void transactionReady(BusMessage bcast);
    // you have the opportunity to initiate a transaction.
    public void startTransaction() {}
    // In start transaction you can use the methods below to send messages.
    protected BusMessage getResponses(BusMessage bcast, BusAggregator aggregator)
    {
        return null;
    }
    protected void pushMessage(BusMessage bcast) {}
    protected MemoryInstruction makeMemoryRequest (BusMessage bcast, MemoryInstruction request) {
        return null;
    }

    protected void sendReply(BusMessage reply) {}
    protected BusMessage getRequest() { return null; }
    protected MemoryInstruction getMemoryResponse() { return null; }
    public void handleRequest(BusMessage request) {}


    // internals
    CyclicBarrier prepBarrier, clockBarrier;
    ProtocolContext<LineState> context;
    public void configure(CyclicBarrier prepBarrier, CyclicBarrier clockBarrier) {
        this.prepBarrier = prepBarrier;
        this.clockBarrier = clockBarrier;
    }
    public void setContext(ProtocolContext<LineState> context) {
        this.context = context;
    }

    // implementing the BusClient interface
    boolean inRecieve, inTransaction;
    BusMessage transactionStarter;
    BusMessage recievedMessage;
    MemoryInstruction memoryResponse;
    BusMessage response;
    MemoryInstruction memoryRequest;
    BusAggregator<BusMessage> aggregator;
    public void recieveMessage(BusMessage b) {
        assert (recievedMessage == null);
        recievedMessage = b;
    }
    public BusMessage getResponse() {
        return response;
    }
    public BusAggregator<BusMessage> getAggregator() {
        return aggregator;
    }
    public BusMessage getBusMessage() {
        inTransaction = true;
        return transactionStarter;
    }
    public MemoryInstruction getMemoryRequest() {
        return memoryRequest;
    }
    public void recieveMemoryResponse(MemoryInstruction resp) {
        memoryResponse = resp;
    }

    public void run() {
        try {
            while(true) {
                prepBarrier.await();
                clockBarrier.await();
                if (recievedMessage != null) {
                    inRecieve = true;
                    handleRequest(recievedMessage);
                    inRecieve = false;
                }
            }
        } catch (InterruptedException e) {
        } catch (BrokenBarrierException e) {
        }
    }

}
