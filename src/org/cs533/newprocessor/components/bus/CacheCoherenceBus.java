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

    @Override
    public String toString() {
        return "CacheCoherenceBus";
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
        BusMessage msg;
        ++runCycles;
        switch (phase) {
            case GetMsg:
                int j = nextClient;
                msg = null;
                do {
                    msg = clients.get(j).getBusMessage();
                    if (msg != null) {
                        Simulator.logEvent("CacheCoherenceBus: Got Message From Client");
                        // Simulator.logEvent("GetMsg - client "+j+" has message");
                        break;
                    }
                    //Simulator.logEvent("GetMsg - client "+j+" not ready");
                    j = (j + 1) % clients.size();
                } while (j != nextClient);
                if (msg == null) {
                    //Simulator.logEvent("GetMsg - none ready");
                    phase = Phase.GetMsg;
                } else {
                    Simulator.logEvent("GetMsg - got message");
                    currentMaster = clients.get(j);
                    nextClient = (j + 1) % clients.size();
                    for (BusClient<BusMessage> client : clients) {
                        if (client != currentMaster) {
                            client.recieveMessage(msg);
                        }
                    }
                    pickNextRound();
                }
                break;
            case GetResponses:
//                // Simulator.logEvent("GetResponses");
                Iterator<BusClient<BusMessage>> iterator = responsesOutstanding.iterator();
                while (iterator.hasNext()) {
                    BusClient<BusMessage> client = iterator.next();
                    BusMessage response = client.getResponse();
                    if (response != null) {
                        Simulator.logEvent("GetResponses - got a response");
                        aggregator.aggregate(response);
                        iterator.remove();
                    }
                }
                if (responsesOutstanding.isEmpty()) {
                    Simulator.logEvent("GetResponses - have all responses");
                    BusMessage response = aggregator.getResult();
                    if (response == null) {
                        throw new java.lang.RuntimeException("Aggregator returned null");
                    }
                    for (BusClient<BusMessage> client : clients) {
                        client.recieveMessage(response);
                    }
                    finishedRound();
                }
                break;
            case GetMemoryResponse:
                Simulator.logEvent("GetMemoryResponses");
                if (upstreamRequest.getIsCompleted()) {
                    for (BusClient<BusMessage> client : clients) {
                        client.recieveMemoryResponse(upstreamRequest);
                    }
                    Simulator.logEvent("GetMemoryResponse - got response");
                    finishedRound();
                }
                break;
            case Delay:
                Simulator.logEvent("Delay");
                if (runCycles >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
                    Simulator.logEvent("Delay - finished");
                    runCycles = 0;
                    phase = Phase.GetMsg;
                }
        }
    }

    private void finishedRound() {
        BusMessage m = currentMaster.getBusMessage();
        if (m != null) {
            for (BusClient<BusMessage> client : clients) {
               if (client != currentMaster) {
                   client.recieveMessage(m);
                }
            }
        }
        pickNextRound();
    }
    private void pickNextRound() {
        aggregator = currentMaster.getAggregator();
        if (aggregator != null) {
            responsesOutstanding = new ArrayList<BusClient<BusMessage>>(clients);
            responsesOutstanding.remove(currentMaster);
            phase = Phase.GetResponses;
            Simulator.logEvent("finishedRound -> GetResponses");
            return;
        }
        upstreamRequest = currentMaster.getMemoryRequest();

        if (upstreamRequest != null) {
            upstream.enqueueMemoryInstruction(upstreamRequest);
            phase = Phase.GetMemoryResponse;
            Simulator.logEvent("finishedRound -> GetMemoryResponse");
            return;
        }
        Simulator.logEvent("finishedRound -> Delay");
        phase = Phase.Delay;
    }

    public void runClock() {
    }

    public int getLatency() {
        return Globals.CACHE_COHERENCE_BUS_LATENCY;
    }
}
