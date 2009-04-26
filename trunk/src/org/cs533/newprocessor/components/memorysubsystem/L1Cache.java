/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayList;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.BusClient;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;
import org.cs533.newprocessor.components.core.ProcessorCore;

/**
 *
 *
 */
public class L1Cache<BusMessage, LineStates,
        Protocol extends CoherenceProtocol<BusMessage,LineStates>>
        implements ComponentInterface, BusClient<BusMessage>, MemoryInterface,
        ProtocolContext<LineStates> {

    ProcessorCore core;
    CacheCoherenceBus<BusMessage> bus;
    ArrayList<MemoryInstruction> pendingRequests;
    Protocol proto;
    public static final int LATENCY = 1;

    public L1Cache (Protocol proto) {
        this.proto = proto;
        proto.setContext(this);
    }

    public MemoryInstruction getNextRequest() {
        return pendingRequests.remove(0);
    }
    LRUEvictHashTable<CacheLine<LineStates>> data;
    public LRUEvictHashTable<CacheLine<LineStates>> getData() {return data;}

    public void enqueueMemoryInstruction(MemoryInstruction request)
    {
        pendingRequests.add(request);
    }

    public L1Cache(ProcessorCore _core, CacheCoherenceBus _bus, Protocol _proto) {
        bus = _bus;
        core = _core;
        proto = _proto;
    }

    public void recieveMessage(BusMessage msg) { proto.recieveMessage(msg);}
    public BusMessage getResponse() {return proto.getResponse();}
    public BusMessage getBusMessage() {return proto.getBusMessage();}
    public BusAggregator<BusMessage> getAggregator() {return proto.getAggregator();}
    public MemoryInstruction getMemoryRequest() {return proto.getMemoryRequest();}
    public void recieveMemoryResponse(MemoryInstruction resp) { proto.recieveMemoryResponse(resp);}
    
    public void runPrep()
    {
        proto.runPrep();
    }

    public void runClock() {
        proto.runClock();
    }

    public int getLatency() {
        return LATENCY;
    }
}
