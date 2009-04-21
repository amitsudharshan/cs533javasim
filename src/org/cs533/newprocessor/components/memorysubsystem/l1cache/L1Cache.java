/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.BusMessage;
import org.cs533.newprocessor.components.bus.BusMessageAggregator;
import org.cs533.newprocessor.components.bus.BusClient;
import org.cs533.newprocessor.components.memorysubsystem.LRUEvictHashTable;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class L1Cache implements ComponentInterface, MemoryInterface, BusClient {

    public class WriteBackMessagePair {

        MemoryInstruction toWriteBack;
        BusMessage message;

        public WriteBackMessagePair(MemoryInstruction toWriteBack, BusMessage message) {
            this.toWriteBack = toWriteBack;
            this.message = message;
        }

        public BusMessage getMessage() {
            return message;
        }

        public MemoryInstruction getToWriteBack() {
            return toWriteBack;
        }
    }

    /* *************************
    These queues are used for communication on the Bus to L2 and between caches
     ************************ */
     /** This is called when the bus presents a message, during runPrep */
    public void recieveMessage(BusMessage msg) {}
    
    public BusMessageAggregator getAggregator() {}
    public BusMessage getBusMessage() {}
    public BusMessage getResponse() {}
    
    
    /** this queue contains messages that we are sending out to the bus and can be read/written during runClock */
    public LinkedBlockingQueue<BusMessage> outMessageQueue = new LinkedBlockingQueue<BusMessage>();
    /** This queue contains messages that we recieve from the bus and can be polled only during runPrep */
    public LinkedBlockingQueue<BusMessage> inMessageQueue = new LinkedBlockingQueue<BusMessage>();
    /* *************************
    Here we store the internal state of the L1Cache
     ************************ */
    /** This is the list of memory instructions which the core has sent in to the cache */
    Vector<MemoryInstruction> instructionsToDo = new Vector<MemoryInstruction>();
    /** This is the list of write back instructions which we are waiting to finish before we can ack the originating message */
    Vector<WriteBackMessagePair> writeBackPairs = new Vector<WriteBackMessagePair>();
    /** This is the backing store for the cache. It is a fully associative cache with LRU replacement */
    LRUEvictHashTable<L1CacheLine> l1BackingStore = new LRUEvictHashTable<L1CacheLine>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
    /** This is the list of messages that we have pulled of the queue in runPrep */
    ArrayList<BusMessage> messagesToDo = new ArrayList<BusMessage>();
    /** This is the memory instruction which the cache is currently working on */
    MemoryInstruction toDo = null;
    /** This is the number of cycles we have delayed while executing this instruction */
    int waitCycles = 0;
    /** This boolean lets us keep track of whether we are processing a
     * memory trasaction between clock ticks.
     */
    boolean isProcessing = false;

    public static int computeNormalizedAddress(int address) {
        return (address / Globals.CACHE_LINE_SIZE) * Globals.CACHE_LINE_SIZE;
    }

    public void runPrep() {
        if (!isProcessing && !instructionsToDo.isEmpty()) {
            toDo = instructionsToDo.firstElement();
            if (toDo != null) {
                isProcessing = true;
                waitCycles = 0;
            }
        } else if (!isProcessing && instructionsToDo.isEmpty()) {
            toDo = null;
        }
        while (inMessageQueue.size() > 0) {
            BusMessage message = inMessageQueue.poll();
            int normAddress = computeNormalizedAddress(message.address);
            if (toDo != null && normAddress == toDo.getInAddress() && message.response == null) {
                /* In this case we are currently processing a memory transaction
                 * with the address associated to the message so we can't ack it
                 * unless the message is a response to something we have sent
                 */
                messagesToDo.add(message);
            }
        }
    }

    public void runClock() {
//        checkWriteBackMessagePairs();
        processMessages();
        if (isProcessing) {
            runMemoryTransaction();
        }
    }

//    public void checkWriteBackMessagePairs() {
//        Iterator<WriteBackMessagePair> pairs = writeBackPairs.iterator();
//        while (pairs.hasNext()) {
//            WriteBackMessagePair pair = pairs.next();
//            if (pair.toWriteBack.getIsCompleted()) {
//                pair.message.response = ResponseTypes.ACK_GET_FROM_MEMORY;
//                outMessageQueue.add(pair.message);
//                pairs.remove();
//            }
//        }
//    }

    public void processMessages() {
        int size = messagesToDo.size();
        for (int i = 0; i < size; i++) {
            /* iterate through the array removing from the head
             * we do this so we can add messages to the tail
             * without looping through them again. */
            BusMessage message = messagesToDo.remove(0);
            int normAdd = computeNormalizedAddress(message.address);
            L1CacheLine line = l1BackingStore.get(normAdd);
            if (line != null) {
                if (message.messageType == MessageTypes.GET_EXCLUSIVE) {
                    if (line.getCurrentState() == L1LineStates.Dirty_Valid) {
                        // WE NEED TO WRITE-BACK and invalidate the line.
                        // Once this is confirmed then we can ack the message.
                        // We force this instruction to execute as the next
                        // Memory Instruction
                        MemoryInstruction memToDo = new MemoryInstruction(normAdd, line.getData(), true);
                        instructionsToDo.add(0, memToDo); // we force this to the head of the l/s queue
                        writeBackPairs.add(new WriteBackMessagePair(memToDo, message));
                        line.setCurrentState(L1LineStates.INVALIDATE_AFTER_WRITE_BACK);
                    } else {
                        //WE HAVE IT BUT IT IS CLEAN SO WE GO INVALID AND ACK
                        line.setCurrentState(L1LineStates.Invalid);
                        message.response = ResponseTypes.ACK;
                    }
                }
            } else {
                //Line not in cache, just ack that we saw the message. Nothing to do here
                message.response = ResponseTypes.ACK;
            }
            if (message.response != null) {
                outMessageQueue.add(message);
            }
        }
    }

    public void runMemoryTransaction() {
        if (isProcessing && waitCycles >= LATENCY) {
            // When we are here we have waited for at least the latency of the L2 cache
            if (evictInstruction != null) {
                if (evictInstruction.getIsCompleted()) {
                    // If we are here, we have an evict instruction we are waiting for...
                    Logger.getAnonymousLogger().info("Running evict instruction with cycle = " + waitCycles);
                    evictInstruction = null;
                    isProcessing = false;
                } else {
                    Logger.getAnonymousLogger().info("Still waiting for memory to come back with cycles = " + waitCycles);
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
                Logger.getAnonymousLogger().info("In write instruction");
                // this is the entry point for a write
                if (!runL2Write()) { //we have an eviction
                    Logger.getAnonymousLogger().info("exexcuted runL2Write, now got back false, so executing eviction");
                    handleEviction();
                } else {
                    Logger.getAnonymousLogger().info("got back true in runL2Write");
                    isProcessing = false;
                }
                toDo.setIsCompleted(true);

                Logger.getAnonymousLogger().info(" set toDo.isCompleted to  " + toDo.getIsCompleted());
            } else if (!toDo.isIsWriteInstruction()) {
                Logger.getAnonymousLogger().info("In read instruction");
                // this is the entry point for a read.
                boolean runMainMemoryRead = !runL2Read();
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

    /**
     *
     * @return false if we have evicted on the store or true if we did not
     */
    public boolean handleCacheReadMiss() {
        l2CacheStore.put(memoryReadInstruction.getInAddress(), new L2CacheLine(memoryReadInstruction.getOutData(), false));
        if (l2CacheStore.address != -1 && l2CacheStore.line.isIsDirty()) {
            handleEviction();
            return false;
        } else {
            return true;
        }

    }

    public void handleEviction() {
        Logger.getAnonymousLogger().info("running eviction in L1");
        BusMessage m = new BusMessage(null, MessageTypes.CACHE_EVICT_WRITE, null, this, l2CacheStore


        evictInstruction = new MemoryInstruction(l2CacheStore.address, l2CacheStore.line.getData(), true);
        l2CacheStore.resetRemoved();
        parentMem.setMemoryInstruction(evictInstruction);
    }

    /**
     *
     * @return first index is true if write hit/false if not. second index
     * is true if eviction and false if not
     */
    public boolean[] runL1Write() {
        int normAdd = computeNormalizedAddress(toDo.getInAddress());
        int offsetAddress = normAdd + (toDo.getInAddress() - normAdd);
        boolean[] toReturn = new boolean[]{false, true}; // default to write miss and no eviction
        if (offsetAddress % 2 != 0) {
            offsetAddress--;
        }
        L1CacheLine line = l1BackingStore.get(normAdd);
        if (line == null) {
            //write miss
            toReturn[0] = false; //write miss
        } else if (line.validForWrite()) {
            // We write the word into the line then store the line.
            for (int i = 0; i < Globals.WORD_SIZE; i++) {
                line.getData()[offsetAddress + i] = toDo.getInData()[i];
            }
            line.setCurrentState(L1LineStates.Dirty_Valid);
            toReturn[0] = true; // write hit.
            l1BackingStore.put(toDo.getInAddress(), line);
            if (l1BackingStore.address != -1 && l1BackingStore.line.getCurrentState() == L1LineStates.Dirty_Valid) {
                toReturn[1] = true; // we need to evict
            }
        }
        Logger.getAnonymousLogger().info("The value of getInAddress is " + toDo.getInAddress() + " and of offset address is " + offsetAddress);
        return toReturn;

    }

    /**
     *
     * @return true if we have a cache hit and false if we have a miss
     */
    public boolean runL1Read() {
        Logger.getAnonymousLogger().info("In runL2Read");
        L1CacheLine line = l1BackingStore.get(toDo.getInAddress());
        Logger.getAnonymousLogger().info("in runL2Read pulled cache line = " + line);
        if (line != null && line.validForRead()) {
            //read hit
            Logger.getAnonymousLogger().info("in runL2Read since line != null we are returning true");
            toDo.setOutData(line.getData());
            return true;
        } else {
            Logger.getAnonymousLogger().info("in runL2Read we are returning false");
            return false;
        }
    }

    public int getLatency() {
        return Globals.L1_CACHE_LATENCY;
    }

    public void enqueueMemoryInstruction(MemoryInstruction instr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
