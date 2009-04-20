/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import org.cs533.newprocessor.components.memorysubsystem.*;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus.BusMessage;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus.MessageTypes;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus.ResponseTypes;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1CacheLine.L1LineStates;

/**
 *
 * @author amit
 */
public class L1Cache implements ComponentInterface, MemoryInterface {

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
        checkWriteBackMessagePairs();
        processMessages();
        if (isProcessing) {
            runMemoryTransaction();
        }
    }

    public void checkWriteBackMessagePairs() {
     //   for(WriteBackMessagePairs pair: )
    }

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
                        MemoryInstruction memToDo = new MemoryInstruction(normAdd, line.getData(), true);
                        instructionsToDo.add(0, memToDo);
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
    }

    public int getLatency() {
        return Globals.L1_CACHE_LATENCY;
    }

    public void setMemoryInstruction(MemoryInstruction instr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
