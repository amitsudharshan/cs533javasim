/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

/**
 *these can handle load instructions of the form
 * load r1 r2 which loads into r2 the value of memory at r1
 * it can also load
 * @author amit
 */
public class LoadInstruction extends AbstractInstruction implements MemoryInstructionInterface {

    public static int opcode = 0x23;
    public static InstructionTypes type = InstructionTypes.memory;
    static final int addressRegMask = 0x03E00000;
    static final int contentRegMask = 0x001F0000;
    static final int addressRegShift = 21;
    static final int contentRegShift = 16;
    /* The values needed by the instruction */
    int registerContent;
    int registerAddress;

    public LoadInstruction() {
    }

    public LoadInstruction(int instruction) {
        setRegisters(instruction);
    }

    public void setRegisters(int instruction) {
        registerAddress = (instruction & addressRegMask) >> addressRegShift;
        registerContent = (instruction & contentRegMask) >> contentRegShift;
    }

    @Override
    public String toString() {
        return "This is a load instruction \n with " +
                "registerAddress = " + registerAddress + " " +
                "and registerContent = " + registerContent;
    }

    @Override
    public int dissasembleInstruction(String instruction) {
        //instruction = "store r1 r2" places M[r1] = r2;
        int instr = 0;
        String[] tokens = instruction.split(" ");
        int reg1 = getIntForRegisterName(tokens[1]);
        int reg2 = getIntForRegisterName(tokens[2]);

        instr = opcode << AbstractInstruction.OP_CODE_SHIFT;
        instr |= reg1 << addressRegShift;
        instr |= reg2 << contentRegShift;
        return instr;
    }

    public MemoryInstruction getMemoryInstruction(RegisterFile rFile) {
        return MemoryInstruction.Load(rFile.getValueForRegister(registerAddress));
    }

    public void handleMemoryInstruction(MemoryInstruction memoryInstruction, RegisterFile rFile) {
        rFile.setValueForRegister(registerContent, byteArrayToInt(memoryInstruction.getOutData()));
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }
}
