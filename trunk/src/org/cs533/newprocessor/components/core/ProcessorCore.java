/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.core;

import org.apache.log4j.Logger;
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

/**
 *
 * @author amit
 */
public class ProcessorCore implements ComponentInterface {

    static Logger logger = Logger.getLogger(ProcessorCore.class.getName());

    public int getLatency() {
        return LATENCY;
    }

    public enum ProcessingStates {

        fetch, decode, alu, memory, writeback, halt
    }

    public int tickCount = 0;
    public static final int LATENCY = 1;
    MemoryInterface mainMemory;
    public RegisterFile rFile = new RegisterFile();
    public ProcessingStates currentState = ProcessingStates.fetch;
    /** This variable indicates a coreID for the chip during simulation */
    public int coreNumber = -1;

    /* State for fetch stage */
    public MemoryInstruction fetchInstruction = null;
    public boolean fetchInstructionCompleted = false;

    /* State for decode stage */
    public int instruction = -1;
    /* State for the execute stage */
    public AbstractInstruction abstrInstr = null;

    /* State for memory stage */
    public MemoryInstruction memoryInstruction = null;
    public boolean memoryInstructionCompleted = false;

    /* State for writeback stage */
    public byte[] toWriteBack;

    /* State if in Halt state */
    public boolean isHalted = false;

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
        tickCount++;
        switch (currentState) {
            case fetch:
                if (fetchInstruction == null) {
                    fetchInstruction = MemoryInstruction.Load(rFile.getPC());
                    mainMemory.enqueueMemoryInstruction(fetchInstruction);
                } else if (fetchInstructionCompleted) {
                    instruction = AbstractInstruction.byteArrayToInt(fetchInstruction.getOutData());
                    //     logger.debug("GOT INSTRUCTION \n" + AbstractInstruction.zeroPadIntForString(instruction, 32) + "\n\n\n");
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
                    rFile.incrementPC(Globals.WORD_SIZE);
                }
                StringBuffer toAppend = new StringBuffer();
                toAppend.append("We have just finished executing:  in coreNumber #" + coreNumber + " with PC value 0x" + Integer.toHexString(rFile.getPC()) + " \n " + abstrInstr.toString() + "\n");
                toAppend.append(rFile.toString() + "\n");
                toAppend.append("--------------------\n");
                logger.debug(toAppend);
                currentState = ProcessingStates.fetch;
                break;
            case halt:
                if (!isHalted) {
                    logger.debug("We have just halted the core #" + coreNumber + " \n " + abstrInstr);
                    logger.debug(rFile);
                    logger.debug("-------------------------");
                }
                isHalted = true;
                break;

        }
    }
}
