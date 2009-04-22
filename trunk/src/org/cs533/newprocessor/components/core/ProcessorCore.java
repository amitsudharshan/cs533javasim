/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.core;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.ALUInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction.InstructionTypes;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.memorysubsystem.MainMemory;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class ProcessorCore implements ComponentInterface {

    public int getLatency() {
        return LATENCY;
    }

    enum ProcessingStates {

        fetch, decode, alu, memory, writeback, halt
    }
    public static final int LATENCY = 1;
    MainMemory mainMemory;
    RegisterFile rFile = new RegisterFile();
    ProcessingStates currentState = ProcessingStates.fetch;

    /* State for fetch stage */
    MemoryInstruction fetchInstruction = null;
    boolean fetchInstructionCompleted = false;

    /* State for decode stage */
    int instruction = -1;

    /* State for the execute stage */
    AbstractInstruction abstrInstr = null;

    /* State for memory stage */
    MemoryInstruction memoryInstruction = null;
    boolean memoryInstructionCompleted = false;

    /* State for writeback stage */
    byte[] toWriteBack;

    /* State if in Halt state */
    boolean isHalted = false;

    public ProcessorCore(int _startPC, MainMemory memory) {
        rFile.setPC(_startPC);
        rFile = new RegisterFile();
        mainMemory = memory;
        Simulator.registerComponent(this);
    }

    public void runPrep() {
        if (fetchInstruction != null) {
            fetchInstructionCompleted = fetchInstruction.getIsCompleted();
        }
        if (memoryInstruction != null) {
            memoryInstructionCompleted = memoryInstruction.getIsCompleted();
        }
    }

    public boolean isHalted() {
        return isHalted;
    }

    public void runClock() {
        switch (currentState) {
            case fetch:
                if (fetchInstruction == null) {
                    fetchInstruction = MemoryInstruction.Load(rFile.getPC());
                    mainMemory.enqueueMemoryInstruction(fetchInstruction);
                } else if (fetchInstructionCompleted) {
                    instruction = AbstractInstruction.byteArrayToInt(fetchInstruction.getOutData());
                    fetchInstruction = null;
                    currentState = ProcessingStates.decode;
                }
                break;
            case decode:
                abstrInstr = AbstractInstruction.getAbstractInstructionForInstruction(instruction);
                if (abstrInstr.getType() == InstructionTypes.halt) {
                    currentState = ProcessingStates.halt;
                }
                currentState = ProcessingStates.alu;
                break;
            case alu:
                if (abstrInstr.getType() == InstructionTypes.alu) {
                    ((ALUInstructionInterface) abstrInstr).executeOperation(rFile);
                }
                currentState = ProcessingStates.memory;
                break;
            case memory:
                if (abstrInstr.getType() == InstructionTypes.memory) {
                    if (memoryInstruction == null) {
                        memoryInstruction = ((MemoryInstructionInterface) abstrInstr).getMemoryInstruction(rFile);
                    } else if (memoryInstructionCompleted) {
                        toWriteBack = memoryInstruction.getOutData();
                        currentState = ProcessingStates.writeback;
                        memoryInstruction = null;
                        memoryInstructionCompleted = false;
                    }
                } else {
                    // this is likely an ALU instruction
                    currentState = ProcessingStates.writeback;
                }
                break;
            case writeback:
                if (abstrInstr.getType() == InstructionTypes.memory) {
                    ((MemoryInstructionInterface) abstrInstr).handleWriteBack(toWriteBack, rFile);
                }
                rFile.incrementPC(32);
                System.out.println("We have just finished executing: \n " + abstrInstr);
                System.out.println(rFile);
                System.out.println("-------------------------");
                currentState = ProcessingStates.fetch;
            case halt:
                System.out.println("We have just halted the core \n " + abstrInstr);
                System.out.println(rFile);
                System.out.println("-------------------------");
                isHalted = true;
                break;
        }
    }
}
