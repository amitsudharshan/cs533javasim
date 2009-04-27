/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

/**
 *these can handle load instructions of the form
 * lw r1 r2 which loads into r2 the value of memory at r1
 * it can also load
 * @author amit
 */
public class LoadWordInstruction extends AbstractInstruction implements MemoryInstructionInterface {

    public static int opcode = 0x23;
    public static InstructionTypes type = InstructionTypes.memory;
    static final int addressRegMask = 0x03E00000;
    static final int contentRegMask = 0x001F0000;
    static final int addressRegShift = 21;
    static final int contentRegShift = 16;
    /* The values needed by the instruction */
    int registerContent;
    int registerAddress;
    int offset;

    public LoadWordInstruction() {
    }

    public LoadWordInstruction(int instruction) {
        setRegisters(instruction);
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new LoadWordInstruction(instruction);
    }

    public void setRegisters(int instruction) {
        registerAddress = (instruction & addressRegMask) >> addressRegShift;
        registerContent = (instruction & contentRegMask) >> contentRegShift;
        offset = (instruction & LOWER_16_IMMEDIATE_MASK);
    }

    @Override
    public String toString() {
        return "This is a load instruction \n with " +
                "registerAddress = " + registerAddress + " " +
                "and registerContent = " + registerContent;
    }

    @Override
    public int assembleInstruction(String instruction) {
        //instruction = "store r1 r2" places M[r1] = r2;
        int instr = 0;
        String[] tokens = instruction.split(" ");
        int reg1 = getIntForRegisterName(tokens[1]);
        int reg2 = getIntForRegisterName(tokens[2]);

        if (tokens.length > 3) {
            offset = signExtendSixteenBitInt(parseImmediate(tokens[3    ]));

        } else {
            offset = 0;
        }
        instr = opcode << AbstractInstruction.OP_CODE_SHIFT;
        instr |= reg1 << addressRegShift;
        instr |= reg2 << contentRegShift;
        instr |= offset;
        return instr;
    }

    public MemoryInstruction getMemoryInstruction(RegisterFile rFile) {
        return MemoryInstruction.Load(rFile.getValueForRegister(registerAddress));
    }

    public void handleWriteBack(byte[] toWriteBack, RegisterFile rFile) {
        rFile.setValueForRegister(registerContent, byteArrayToInt(toWriteBack));
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }
}
