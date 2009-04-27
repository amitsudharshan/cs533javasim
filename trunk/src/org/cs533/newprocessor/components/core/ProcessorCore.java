/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.core;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.abstractandinterface.ALUInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction.InstructionTypes;
import org.cs533.newprocessor.assembler.abstractandinterface.BranchInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;
import org.cs533.newprocessor.simulator.Simulator;

/*
 * new CacheCoherenceBus<MIProtocol.MIBusMessage>(mainMemory)
(10:14:41 PM) brandon.cs533@gmail.com: new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>()
(10:14:59 PM) brandon.cs533@gmail.com: bus.registerClient(l1Cache)
(10:15:12 PM) brandon.cs533@gmail.com: oh, except it looks like the bus doesn't have a constructor yet
 * */

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
    MemoryInterface mainMemory;
    RegisterFile rFile = new RegisterFile();
    ProcessingStates currentState = ProcessingStates.fetch;
    /** This variable indicates a coreID for the chip during simulation */
    int coreNumber = -1;

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

    public ProcessorCore(int _startPC, MemoryInterface memory, int coreNumber_) {
        rFile.setPC(_startPC);
        mainMemory = memory;
        coreNumber = coreNumber_;
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
                    System.out.println("GOT INSTRUCTION \n" + AbstractInstruction.zeroPadIntForString(instruction, 32) + "\n\n\n");
                    fetchInstruction = null;
                    currentState = ProcessingStates.decode;
                }
                break;
            case decode:
                abstrInstr = AbstractInstruction.getAbstractInstructionForInstruction(instruction);
                if (abstrInstr.getType() == InstructionTypes.halt) {
                    currentState = ProcessingStates.halt;
                } else if (abstrInstr.getType() == InstructionTypes.alu) {
                    currentState = ProcessingStates.alu;
                } else if (abstrInstr.getType() == InstructionTypes.memory) {
                    currentState = ProcessingStates.memory;
                } else {
                    currentState = ProcessingStates.alu;
                }
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
                        mainMemory.enqueueMemoryInstruction(memoryInstruction);
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
                if (abstrInstr.getType() == InstructionTypes.branch) {
                    ((BranchInstructionInterface) abstrInstr).setPC(rFile);
                } else {
                    rFile.incrementPC(8 * Globals.WORD_SIZE);
                }
                System.out.println("We have just finished executing:  in coreNumber #" + coreNumber + " with PC value 0x" + Integer.toHexString(rFile.getPC()) + " \n " + abstrInstr.toString());
                System.out.println(rFile);
                System.out.println("--------------------");
                currentState = ProcessingStates.fetch;
                break;
            case halt:
                if (!isHalted) {
                    System.out.println("We have just halted the core #" + coreNumber + " \n " + abstrInstr);
                    System.out.println(rFile);
                    System.out.println("-------------------------");
                }
                isHalted = true;
                break;

        }
    }
}
