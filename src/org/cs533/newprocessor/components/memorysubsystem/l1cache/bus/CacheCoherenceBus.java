/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.l2cache.FullyAssociativeCache;

/**
 *
 * @author amit
 */
public class CacheCoherenceBus //implements ComponentInterface {
{
//    public enum MessageTypes {
//
//        CACHE_MISS_READ, CACHE_EVICT_WRITE, GET_EXCLUSIVE
//    }
//
//    public enum ResponseTypes {
//
//        ACK, ACK_GET_FROM_MEMORY
//    }
//
//    public class BusMessage implements Comparable {
//
//        public BusMessage messageFromSender = null;
//        public MessageTypes messageType = null;
//        public ResponseTypes response = null;
//        public L1Cache l1Cache = null; // the L1Cache originating the message.
//        public int address = -1; // the address we are searching for (NORMALIZED...)
//        public byte[] inData = null; // Data we may be writing
//        public byte[] outData = null; // Data we may need to get out of this message
//        private int waitCycles = 0;
//
//        public BusMessage(BusMessage messageFromSender, MessageTypes messageType, ResponseTypes response, L1Cache l1Cache, int address, byte[] inData, byte[] outData) {
//            this.messageFromSender = messageFromSender;
//            this.messageType = messageType;
//            this.response = response;
//            this.l1Cache = l1Cache;
//            this.address = address;
//            this.inData = inData;
//            this.outData = outData;
//        }
//
//        public BusMessage(BusMessage origMessage, L1Cache newCache) {
//            messageFromSender = origMessage;
//            address = origMessage.address;
//            l1Cache = newCache;
//        }
//
//        public int compareTo(Object o) {
//            return new Integer(waitCycles).compareTo((Integer) o);
//        }
//    }
//    ArrayList<L1Cache> l1Caches;
//    FullyAssociativeCache l2Cache;
//    ArrayList<BusMessage> messagesToBroadcast = new ArrayList<BusMessage>();
//    ArrayList<BusMessage> waitingForAck = new ArrayList<BusMessage>();
//    HashMap<BusMessage, HashSet<BusMessage>> queuedAcks = new HashMap<BusMessage, HashSet<BusMessage>>();
//    PriorityQueue<BusMessage> waitingForL2 = new PriorityQueue<BusMessage>();
//    BusMessage toDoFromL2 = null;
//    MemoryInstruction toDo = null;
//    int waitCyclesOnL2Resp = 0;
//    boolean processingL2 = false;
//
//    public void registerCache(L1Cache l1Cache) {
//        if (!l1Caches.contains(l1Cache)) {
//            l1Caches.add(l1Cache);
//        }
//    }
//
//    public void runPrep() {
//        // for every cache pull the outbound messages from the queue and prepare
//        // to broadcast them
//        for (L1Cache cache : l1Caches) {
//            BusMessage message = null;
//            while ((message = cache.outMessageQueue.poll()) != null) {
//                if (message.response == null && message.messageType == MessageTypes.GET_EXCLUSIVE) {
//                    // THESE ARE INCOMING MESSAGES THAT NEED TO BE BROADCAST
//                    messagesToBroadcast.add(message);
//                } else if (message.response == null && message.messageType == MessageTypes.CACHE_MISS_READ || message.messageType == MessageTypes.CACHE_EVICT_WRITE) {
//                    // THESE ARE INCOMING MESSAGES THAT NEED AN L2 Event
//                    waitingForL2.add(message);
//                } else if (message.response != null && message.messageFromSender != null) {
//                    // THESE ARE ACKS THAT NEED TO BE HELD UNTIL ALL ACKS ARRIVE
//                    HashSet<BusMessage> resp = queuedAcks.get(message.messageFromSender);
//                    if (resp == null) {
//                        resp = new HashSet<BusMessage>();
//                    }
//                    resp.add(message);
//                    queuedAcks.put(message.messageFromSender, resp);
//                }
//            }
//        }
//        if (!processingL2) {
//            /* This will pull the next L2MemInstr. off the queue
//             * which is ordered by waitTime
//             */
//            toDoFromL2 = waitingForL2.poll();
//            toDo = null;
//            if (toDoFromL2 != null) {
//                processingL2 = true;
//                waitCyclesOnL2Resp = 0;
//            }
//        }
//    }
//
//    public void runClock() {
//        incrementQueueWaitCycleCount();
//        sendBroadcastMessages();
//        // increment cycle count for every message in the ackqueue
//        checkAckQueueForResponse();
//        if (processingL2) {
//            runL2Transaction();
//        }
//    }
//
//    public void runL2Transaction() {
//        if (toDo == null && toDoFromL2.waitCycles++ >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
//            // HERE WE ARE GOING TO Queue the L2 transaction AFTER WAITING ENOUGH BUS CYCLES
//            if (toDoFromL2.messageType == MessageTypes.CACHE_MISS_READ) {
//                //read instruction on L1 miss
//                toDo = MemoryInstruction.Load(toDoFromL2.address);
//            } else if (toDoFromL2.messageType == MessageTypes.CACHE_EVICT_WRITE) {
//                // write instruction on L1 evict
//                toDo = MemoryInstruction.Store(toDoFromL2.address, toDoFromL2.inData);
//            }
//            l2Cache.enqueueMemoryInstruction(toDo);
//        } else if (toDo != null && toDo.getIsCompleted() && waitCyclesOnL2Resp++ >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
//            /* Once we have sent the memory instruction L2, we have to wait
//            till it is completed, and then we have to wait for the latency
//            on the bus before responding
//             */
//            toDoFromL2.outData = toDo.getOutData();
//            toDoFromL2.response = ResponseTypes.ACK;
//            toDoFromL2.l1Cache.inMessageQueue.add(toDoFromL2);
//            processingL2 = false;
//        }
//
//    }
//
//    public void incrementQueueWaitCycleCount() {
//        for (HashSet<BusMessage> messages : queuedAcks.values()) {
//            for (BusMessage message : messages) {
//                message.waitCycles++;
//            }
//
//        }
//        int counter = waitingForL2.size();
//        for (int i = 0; i <
//                counter; i++) {
//            BusMessage message = waitingForL2.poll();
//            message.waitCycles++;
//            waitingForL2.add(message); // you have to re-add to the queue.
//        }
//
//    }
//
//    private void checkAckQueueForResponse() {
//        int cacheSize = l1Caches.size();
//        Iterator<BusMessage> iter = queuedAcks.keySet().iterator();
//        while (iter.hasNext()) {
//            HashSet<BusMessage> messages = queuedAcks.get(iter.next());
//            if (messages.size() == cacheSize - 1) { // we wait until all caches (except for originating cache) have ack'd
//                // Here we have to wait until we have delayed enough cycles for each message on the bus
//                int minCycles = Integer.MIN_VALUE;
//                for (BusMessage testCycle : messages) {
//                    minCycles = Math.min(minCycles, testCycle.waitCycles);
//                }
//
//                BusMessage origMessage = messages.iterator().next().messageFromSender;
//                if (minCycles >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
//                    Iterator<BusMessage> msgIter = messages.iterator();
//                    while (msgIter.hasNext()) {
//                        BusMessage next = msgIter.next();
//                        if (next.response == ResponseTypes.ACK_GET_FROM_MEMORY) { // if any of the caches had it in memory then we need to let orig. cache know
//                            origMessage.response = next.response;
//                        }
//
//                    }
//                    iter.remove(); // this messages has been proccessed so we don't need to keep track of it any longer.
//                    origMessage.l1Cache.inMessageQueue.add(origMessage);
//                }
//
//            }
//        }
//    }
//
//    public void sendBroadcastMessages() {
//        while (messagesToBroadcast.size() > 0) {
//            BusMessage message = messagesToBroadcast.get(0);
//            if (message.waitCycles++ >= Globals.CACHE_COHERENCE_BUS_LATENCY) {
//                for (L1Cache cache : l1Caches) {
//                    if (message.l1Cache != cache) {
//                        BusMessage responseMessage = new BusMessage(message, cache);
//                        cache.inMessageQueue.add(responseMessage);
//                        waitingForAck.add(message);
//                    }
//
//                }
//            }
//        }
//    }
//
//    public int getLatency() {
//        return Globals.CACHE_COHERENCE_BUS_LATENCY;
//    }
}
