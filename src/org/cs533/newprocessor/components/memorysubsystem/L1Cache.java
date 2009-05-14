/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.BusClient;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;
import org.cs533.newprocessor.components.core.ProcessorCore;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 *
 */
public class L1Cache<BusMessage, LineStates extends LineState, Protocol extends CoherenceProtocol<BusMessage, LineStates>>
        implements ComponentInterface, BusClient<BusMessage>, MemoryInterface,
        ProtocolContext<LineStates> {


    static Logger logger = Logger.getLogger(L1Cache.class.getName());
    ProcessorCore core;
    CacheCoherenceBus<BusMessage> bus;
    ArrayList<MemoryInstruction> pendingRequests = new ArrayList<MemoryInstruction>();
    Protocol proto;
    public static final int LATENCY = Globals.L1_CACHE_LATENCY;

    public L1Cache(Protocol proto) {
        this.proto = proto;
        data = new LRUEvictHashTable<CacheLine<LineStates>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
        proto.setContext(this);
        Simulator.registerComponent(this);
    }

    public MemoryInstruction getNextRequest() {
        if (pendingRequests.isEmpty()) {
            return null;

        } else {
            return pendingRequests.remove(0);

        }
    }
    LRUEvictHashTable<CacheLine<LineStates>> data;

    public LRUEvictHashTable<CacheLine<LineStates>> getData() {
        return data;
    }

    public void enqueueMemoryInstruction(MemoryInstruction request) {
        pendingRequests.add(request);
        logger.debug(request);
    }

    public L1Cache(ProcessorCore _core, CacheCoherenceBus _bus, Protocol _proto) {
        bus = _bus;
        core = _core;
        proto = _proto;
    }

    public void recieveMessage(BusMessage msg) {
        proto.recieveMessage(msg);
    }

    public BusMessage getResponse() {
        return proto.getResponse();
    }

    public BusMessage getBusMessage() {
        return proto.getBusMessage();
    }

    public BusAggregator<BusMessage> getAggregator() {
        return proto.getAggregator();
    }

    public MemoryInstruction getMemoryRequest() {
        return proto.getMemoryRequest();
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        proto.recieveMemoryResponse(resp);
    }

    public void runPrep() {
        proto.runPrep();
    }

    public void runClock() {
        proto.runClock();
    }

    public int getLatency() {
        return LATENCY;
    }
}
