/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.cs533.newprocessor.components.memorysubsystem.*;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus.BusMessage;

/**
 *
 * @author amit
 */
public class L1Cache implements ComponentInterface, MemoryInterface {

    /* *************************
    These queues are used for communication on the Bus to L2 and between caches
     ************************ */
    /** this queue contains messages that we are sending out to the bus and can be read/written during runClock */
    public LinkedBlockingQueue<BusMessage> outMessageQueue = new LinkedBlockingQueue<BusMessage>();
    /** This queue contains messages that we recieve from the bus and can be polled only during runPrep */
    public LinkedBlockingQueue<BusMessage> inMessageQueue = new LinkedBlockingQueue<BusMessage>();
    /* *************************
    Here we store the internal state of the L1Cache
     ************************ */
    /** This is the queue of memory instructions which the core has sent in to the cache */
    LinkedBlockingQueue<MemoryInstruction> instructionsToDo = new LinkedBlockingQueue<MemoryInstruction>();
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
        if (!isProcessing) {
            toDo = instructionsToDo.poll();
            if (toDo != null) {
                isProcessing = true;
                waitCycles = 0;
            }
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
        processMessages();
        if (isProcessing) {
            runMemoryTransaction();
        }
    }

    public void processMessages() {
    }

    public void runMemoryTransaction() {
    }

    public int getLatency() {
        return Globals.L1_CACHE_LATENCY;
    }

    public void setMemoryInstruction(MemoryInstruction instr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
