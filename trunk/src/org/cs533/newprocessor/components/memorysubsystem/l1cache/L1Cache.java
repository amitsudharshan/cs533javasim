/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import java.util.ArrayList;
import org.cs533.newprocessor.components.memorysubsystem.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.memorysubsystem.LRUEvictHashTable;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols.AbstractProtocol;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols.AbstractProtocol.BusMessage;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class L1Cache implements ComponentInterface, MemoryInterface {

    MemoryInterface parentMem;
    public static int LATENCY = Globals.L1_CACHE_LATENCY; // MADE UP VALUE HERE
    LinkedBlockingQueue<MemoryInstruction> queue = new LinkedBlockingQueue<MemoryInstruction>();
    LinkedBlockingQueue<BusMessage> messagesFromBus = new LinkedBlockingQueue<BusMessage>();
    /* internal state */
    int size = Globals.L1_SIZE_IN_NUMBER_OF_LINES;
    LRUEvictHashTable l1CacheStore = null;
    MemoryInstruction toDo;
    boolean isProcessing = false;
    MemoryInstruction evictInstruction = null;
    MemoryInstruction memoryReadInstruction = null;
    int waitCycles = 0;
    int readMissCounter = 0;
    AbstractProtocol protocol;

    public void setMemoryInstruction(MemoryInstruction instr) {
        queue.add(instr);
    }

    public L1Cache(MemoryInterface memInterface_, AbstractProtocol protocol_) {
        parentMem = memInterface_;
        protocol = protocol_;
        l1CacheStore = new LRUEvictHashTable(size);
        Simulator.registerComponent(this);
    }

    public L1Cache(int sizeInLines, int latency, MemoryInterface memInterface_, AbstractProtocol protocol_) {
        parentMem = memInterface_;
        size = sizeInLines;
        LATENCY = latency;
        protocol = protocol_;
        l1CacheStore = new LRUEvictHashTable(size);
        Simulator.registerComponent(this);
    }

    public void runPrep() {
        if (!isProcessing) {
            toDo = queue.poll();
            Logger.getAnonymousLogger().info(" Ran runPrep!!! and got back a value of " + toDo);
            if (toDo != null) {
                isProcessing = true;
            }
            waitCycles = 0;
            readMissCounter = 0;
            evictInstruction = null;
            memoryReadInstruction = null;

        }
    }

    public void runClock() {
        int doneMessages = 0;
        while (doneMessages++ < Globals.MAX_NUMBER_BUS_MESSAGES_PER_CYCLE) {
            BusMessage message = messagesFromBus.poll();
            if (message == null) {
                break;
            }
            if (toDo != null && message.getAddress() == toDo.getInAddress()) {
                messagesFromBus.add(message);
            } else {
                handleMessage(message);
            }
        }
        if (isProcessing && waitCycles >= LATENCY) {
            // When we are here we have waited for at least the latency of the L1 cache
            if (evictInstruction != null) {
                if (evictInstruction.getIsCompleted()) {
                    // If we are here, we have an evict instruction we are waiting for...
                    Logger.getAnonymousLogger().info("in L1: Running evict instruction with cycle = " + waitCycles);
                    evictInstruction = null;
                    isProcessing = false;
                } else {
                    Logger.getAnonymousLogger().info("in L1: Still waiting for memory to come back with cycles = " + waitCycles);
                }
            } else if (memoryReadInstruction != null) {
                if (memoryReadInstruction.getIsCompleted() && readMissCounter++ >= LATENCY) {
                    // if we are here we are waiting to get back data from a cache read miss.
                    Logger.getAnonymousLogger().info("Running readMiss instrcution with waitCycles = " + waitCycles + " and readMissCounter = " + readMissCounter);
                    boolean notEvicted = handleCacheReadMiss();
                    if (notEvicted) {
                        isProcessing = false;
                    }
                    toDo.setOutData(memoryReadInstruction.getOutData());
                    toDo.setIsCompleted(true);
                    memoryReadInstruction = null;
                }
            } else if (toDo.isIsWriteInstruction()) {
                Logger.getAnonymousLogger().info("L1: In write instruction");
                // this is the entry point for a write
                if (!runL1Write()) { //we have an eviction
                    Logger.getAnonymousLogger().info("exexcuted runL1Write, now got back false, so executing eviction");
                    handleEviction();
                } else {
                    Logger.getAnonymousLogger().info("got back true in runL1Write");
                    isProcessing = false;
                }
                toDo.setIsCompleted(true);

                Logger.getAnonymousLogger().info(" set toDo.isCompleted to  " + toDo.getIsCompleted());
            } else if (!toDo.isIsWriteInstruction()) {
                Logger.getAnonymousLogger().info("In read instruction");
                // this is the entry point for a read.
                boolean runMainMemoryRead = !runL1Read();
                if (runMainMemoryRead) {
                    memoryReadInstruction = new MemoryInstruction(toDo.getInAddress(), toDo.getInData(), false);
                    parentMem.setMemoryInstruction(memoryReadInstruction);
                } else {
                    toDo.setIsCompleted(true);
                    isProcessing = false;
                }
            }
        } else {
            Logger.getAnonymousLogger().info("fell through if statement with waitCycles =  " + waitCycles + " and readMissCounter = " + readMissCounter);
        }
        waitCycles++;
    }

    public void handleMessage(BusMessage message) {
        Object result = l1CacheStore.get(message.getAddress());
        L1CacheLine line = null;
        if (result != null) {
            line = (L1CacheLine) result;
        }
        protocol.handleMessage(message, line);
    }

    /**
     *
     * @return false if we have evicted on the store or true if we did not
     */
    public boolean handleCacheReadMiss() {
        l1CacheStore.put(memoryReadInstruction.getInAddress(), new L1CacheLine(memoryReadInstruction.getOutData(), false));
        if (l1CacheStore.address != -1 && l1CacheStore.line.isIsDirty()) {
            handleEviction();
            return false;
        } else {
            return true;
        }

    }

    public void handleEviction() {
        Logger.getAnonymousLogger().info("running eviction in L1");
        evictInstruction =
                new MemoryInstruction(l1CacheStore.address, l1CacheStore.line.getData(), true);
        l1CacheStore.resetRemoved();
        parentMem.setMemoryInstruction(evictInstruction);
    }

    /**
     *
     * @return false if we are going to evict and need to run an eviction,
     * or true if we succesfully wrote in with no need to evict
     */
    public boolean runL1Write() {
        L1CacheLine line = new L1CacheLine(toDo.getInData(), true);
        Logger.getAnonymousLogger().info("The value of getInAddress is " + toDo.getInAddress());
        l1CacheStore.put(toDo.getInAddress(), line);
        if (l1CacheStore.address != -1 && l1CacheStore.line.isIsDirty()) {
            return false;
        } else {
            return true;
        }

    }

    /**
     *
     * @return true if we have a cache hit and false if we have a miss
     */
    public boolean runL1Read() {
        Logger.getAnonymousLogger().info("In runL1Read");
        L1CacheLine line = (L1CacheLine) l1CacheStore.get(toDo.getInAddress());
        Logger.getAnonymousLogger().info("in runL1Read pulled cache line = " + line);
        if (line != null) {
            Logger.getAnonymousLogger().info("in runL1Read since line != null we are returning true");
            toDo.setOutData(line.getData());
            return true;
        } else {
            Logger.getAnonymousLogger().info("in runL1Read we are returning false");
            return false;
        }

    }

    public int getLatency() {
        return LATENCY;
    }
}
