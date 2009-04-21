/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.concurrent.LinkedBlockingQueue;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class MainMemory implements ComponentInterface,MemoryInterface {

    public static final int LATENCY = Globals.MAIN_MEMORY_LATENCY; // MADE UP VALUE HERE
    /* input port */
    LinkedBlockingQueue<MemoryInstruction> inQueue;
    /* state variables */
    MemoryInstruction toDo;
    byte[] memory = new byte[Globals.TOTAL_MEMORY_SIZE]; // 512K main memory
    int waitCycles = 0;
    boolean isProcessing = false;

    public MainMemory() {
        inQueue = new LinkedBlockingQueue<MemoryInstruction>();
        Simulator.registerComponent(this);
    }

    public void enqueueMemoryInstruction(MemoryInstruction instruction) {
        inQueue.add(instruction);
    }

    public void runPrep() {
        if (!isProcessing) {
            toDo = inQueue.poll();
            if (toDo != null) {
                isProcessing = true;
                waitCycles = 0;
            }
        }
    }

    public void runClock() {
        if (isProcessing) {
            if (waitCycles++ == getLatency()) {
                runMemoryInstruction();
                isProcessing = false;
                toDo.setIsCompleted(true);
            }
        }
    }

    public void runMemoryInstruction() {
        if (toDo.isIsWriteInstruction()) {
            int counter = 0;
            for (int i = toDo.getInAddress(); i < toDo.getInData().length; i++) {
                memory[i] = toDo.getInData()[counter++];
            }
        } else {
            toDo.outData = new byte[Globals.CACHE_LINE_SIZE];
            for (int i = 0; i < Globals.CACHE_LINE_SIZE; i++) {
                if (toDo.getInAddress() + i < memory.length) {
                    toDo.outData[i] = memory[toDo.getInAddress() + i];
                }
            }
        }
    }

    public int getLatency() {
        return LATENCY;
    }

    public static void main(String[] args) {
        MainMemory m = new MainMemory();
        byte[] b = new byte[Globals.CACHE_LINE_SIZE];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }
        MemoryInstruction instruction = new MemoryInstruction(0, b, true);
        m.enqueueMemoryInstruction(instruction);
        m.runPrep();
        while (!instruction.isCompleted) {
            m.runClock();
        }
        System.out.println(m.getPrintableMemoryLine(52));
        System.out.println(m);
    }

    public String getPrintableMemoryLine(int address) {
        return "The value of memory at address = 0x" + Integer.toHexString(address) + " is " + memory[address];
    }

    @Override
    public String toString() {
        StringBuffer toWrite = new StringBuffer();
        toWrite.append("PRINTING CONTENTS OF MAIN MEMORY AT " + System.currentTimeMillis() + ":: \n\n");
        int counter = 0;
        toWrite.append("\nMEMORY AT: 0x0:      ");
        for (int i = 0; i < memory.length; i++) {
            toWrite.append("||" + memory[i] + "||");
            if (++counter == 4) {
                counter = 0;
                toWrite.append("\nMEMORY AT: 0x" + Integer.toHexString(i + 1) + ":      ");
            }
        }
        return toWrite.toString();
    }
}
