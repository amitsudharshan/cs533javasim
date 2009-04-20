/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class L2Cache implements ComponentInterface {

    MainMemory mainMemory;
    public static final int LATENCY = Globals.L2_CACHE_LATENCY; // MADE UP VALUE HERE
    LinkedBlockingQueue<MemoryInstruction> queue = new LinkedBlockingQueue<MemoryInstruction>();

    /* internal state */
    L2CacheBackingStore l2CacheStore = new L2CacheBackingStore(Globals.L2_SIZE_IN_NUMBER_OF_LINES); // Is of type <Integer,byte[]>
    MemoryInstruction toDo;
    boolean isProcessing = false;
    MemoryInstruction evictInstruction = null;
    MemoryInstruction memoryReadInstruction = null;
    int waitCycles = 0;
    int readMissCounter = 0;

    public void setMemoryInstruction(MemoryInstruction instr) {
        queue.add(instr);
    }

    public L2Cache(MainMemory _mainMemory) {
        mainMemory = _mainMemory;
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
        if (isProcessing && waitCycles >= LATENCY) {
            // When we are here we have waited for at least the latency of the L2 cache
            if (evictInstruction != null && evictInstruction.getIsCompleted()) {
                // If we are here, we have an evict instruction we are waiting for...
                Logger.getAnonymousLogger().info("Running evict instruction with cycle = " + waitCycles);
                evictInstruction = null;
                isProcessing = false;
            } else if (memoryReadInstruction != null && memoryReadInstruction.getIsCompleted() && readMissCounter++ >= LATENCY) {
                // if we are here we are waiting to get back data from a cache read miss.
                Logger.getAnonymousLogger().info("Running readMiss instrcution with waitCycles = " + waitCycles + " and readMissCounter = " + readMissCounter);
                boolean notEvicted = handleCacheReadMiss();
                if (notEvicted) {
                    toDo.setOutData(memoryReadInstruction.getOutData());
                    toDo.setIsCompleted(true);
                    isProcessing = false;
                }
            } else if (toDo.isIsWriteInstruction()) {
                Logger.getAnonymousLogger().info("In write instruction");
                // this is the entry point for a write
                if (!runL2Write()) { //we have an eviction
                    Logger.getAnonymousLogger().info("exexcuted runL2Write, now got back false, so executing eviction");
                    handleEviction();
                } else {
                    Logger.getAnonymousLogger().info("got back true in runL2Write");
                    isProcessing = false;
                }
                toDo.isCompleted = true;

                Logger.getAnonymousLogger().info(" set toDo.isCompleted to  " + toDo.getIsCompleted());
            } else if (!toDo.isIsWriteInstruction()) {
                Logger.getAnonymousLogger().info("In read instruction");
                // this is the entry point for a read.
                boolean runMainMemoryRead = !runL2Read();
                if (runMainMemoryRead) {
                    memoryReadInstruction = new MemoryInstruction(toDo.getInAddress(), toDo.getInData(), false);
                    mainMemory.setMemoryInstruction(memoryReadInstruction);
                }else {
                    toDo.isCompleted = true;
                    isProcessing = false;
                }
            }
        } else {
            Logger.getAnonymousLogger().info("fell through if statement with waitCycles =  " + waitCycles + " and readMissCounter = " + readMissCounter);
        }
        waitCycles++;
    }

    /**
     *
     * @return false if we have evicted on the store or true if we did not
     */
    public boolean handleCacheReadMiss() {
        l2CacheStore.put(memoryReadInstruction.getInAddress(), new CacheLine(memoryReadInstruction.getOutData(), false));
        memoryReadInstruction = null;
        if (l2CacheStore.address != -1) {
            handleEviction();
            return false;
        } else {
            return true;
        }
    }

    public void handleEviction() {
        Logger.getAnonymousLogger().info("running eviction in L2");
        evictInstruction = new MemoryInstruction(l2CacheStore.address, l2CacheStore.line.getData(), true);
        l2CacheStore.resetRemoved();
        mainMemory.setMemoryInstruction(evictInstruction);
    }

    /**
     *
     * @return false if we are going to evict and need to run an eviction,
     * or true if we succesfully wrote in with no need to evict
     */
    public boolean runL2Write() {
        CacheLine line = new CacheLine(toDo.getInData(), true);
         Logger.getAnonymousLogger().info("The value of getInAddress is " + toDo.getInAddress());
        l2CacheStore.put(toDo.getInAddress(), line);
        if (l2CacheStore.address != -1 && l2CacheStore.line.isIsDirty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @return true if we have a cache hit and false if we have a miss
     */
    public boolean runL2Read() {
        Logger.getAnonymousLogger().info("In runL2Read");
        CacheLine line = (CacheLine) l2CacheStore.get(toDo.getInAddress());
        Logger.getAnonymousLogger().info("in runL2Read pulled cache line = " + line);
        if (line != null) {
            Logger.getAnonymousLogger().info("in runL2Read since line != null we are returning true");
            toDo.setOutData(line.getData());
            return true;
        } else {
            Logger.getAnonymousLogger().info("in runL2Read we are returning false");
            return false;
        }
    }

    public int getLatency() {
        return LATENCY;
    }
}
