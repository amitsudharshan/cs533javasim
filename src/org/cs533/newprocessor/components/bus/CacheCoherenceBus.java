/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.bus;

import java.util.ArrayList;
import java.util.Iterator;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.simulator.Simulator;

/**
 * The cache coherence bus routes messages between sibling caches
 * and the higher level. 
 * @author brandon
 */
public class CacheCoherenceBus<BusMessage> implements ComponentInterface {

    public enum MessageTypes {
        CACHE_MISS_READ, CACHE_EVICT_WRITE, GET_EXCLUSIVE
    }

    public enum ResponseTypes {
        ACK, ACK_GET_FROM_MEMORY
    }

    enum Phase {
        GetMsg, BroadcastMsg, GetResponses, GetMemoryResponse, BroadcastResponse, Delay
    }

    ArrayList<BusClient<BusMessage>> clients;
    MemoryInterface upstream;
    BusAggregator<BusMessage> aggregator;
    BusClient<BusMessage> currentMaster;
    Phase phase = Phase.GetMsg;
    ArrayList<BusClient<BusMessage>> responsesOutstanding;
    MemoryInstruction upstreamRequest;
    int nextClient = 0;
    BusMessage msg = null;
    int runCycles = 0;

    public CacheCoherenceBus(MemoryInterface upstream) {
        this.upstream = upstream;
        this.phase = Phase.GetMsg;
        clients = new ArrayList<BusClient<BusMessage>>();
        Simulator.registerComponent(this);
    }

    public void registerClient(BusClient<BusMessage> client) {
        if (!clients.contains(client)) {
            clients.add(client);
        }
    }

    public void runPrep() {
        // for every cache pull the outbound messages from the queue and prepare
        // to broadcast them
        ++runCycles;
        switch (phase) {
            case GetMsg:
                int j = nextClient;
                msg = null;
                do {
                    msg = clients.get(j).getBusMessage();
                    if (msg != null) {
                        break;
                    }
                    j = (j + 1) % clients.size();
                } while (j != nextClient);
                if (msg == null) {
                    phase = Phase.Delay;
                } else {
                    currentMaster = clients.get(j);
                    nextClient = (nextClient + 1) % clients.size();
                    for (BusClient<BusMessage> client : clients) {
                        if (client != currentMaster) {
                            client.recieveMessage(msg);
                        }
                    }
                    finishedRound();
                }
                break;
            case GetResponses:
                Iterator<BusClient<BusMessage>> iterator = responsesOutstanding.iterator();
                while (iterator.hasNext()) {
                    BusClient<BusMessage> client = iterator.next();
                    BusMessage response = client.getResponse();
                    if (response != null) {
                        aggregator.aggregate(response);
                        iterator.remove();
                    }
                }
                if (responsesOutstanding.isEmpty()) {
                    BusMessage response = aggregator.getResult();
                    for (BusClient<BusMessage> client : clients) {
                        client.recieveMessage(response);
                    }
                    finishedRound();
                }
                break;
            case GetMemoryResponse:
                if (upstreamRequest.getIsCompleted()) {
                    for (BusClient<BusMessage> client : clients) {
                        client.recieveMemoryResponse(upstreamRequest);
                    }
                    finishedRound();
                }
                break;
            case Delay:
                if (runCycles >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
                    runCycles = 0;
                    phase = Phase.GetMsg;
                }
        }
    }

    private void finishedRound() {
        aggregator = currentMaster.getAggregator();
        if (aggregator != null) {
            responsesOutstanding = new ArrayList<BusClient<BusMessage>>(clients);
            phase = Phase.GetResponses;
            return;
        }
        upstreamRequest = currentMaster.getMemoryRequest();
        if (upstreamRequest != null) {
            upstream.enqueueMemoryInstruction(upstreamRequest);
            phase = Phase.GetMemoryResponse;
            return;
        }
        phase = Phase.Delay;
    }

    public void runClock() {
    }

    public int getLatency() {
        return Globals.CACHE_COHERENCE_BUS_LATENCY;
    }
}
