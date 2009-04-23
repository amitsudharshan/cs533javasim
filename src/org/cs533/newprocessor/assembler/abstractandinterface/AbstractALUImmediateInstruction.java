/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.abstractandinterface;

import org.cs533.newprocessor.assembler.abstractandinterface.ALUInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;

/**
 *
 * @author amit
 */
public abstract class AbstractALUImmediateInstruction extends AbstractInstruction implements ALUInstructionInterface {

    InstructionTypes type = InstructionTypes.alu;
    int opcode;
    String descriptor;
    public int sourceRegister;
    public int destinationRegister;
    public int immediate;

    public AbstractALUImmediateInstruction(int opcode_, String descriptor_) {
        opcode = opcode_;
        descriptor = descriptor_;
    }

    /*decoding masks */
    static final int sourceMask = 0x03E00000;
    static final int destinationMask = 0x001F0000;

    /* Shift bits */
    static final int sourceShift = 21;
    static final int destShift = 16;

    public void setRegisters(int instruction) {

        int masked = instruction & sourceMask;
        sourceRegister = masked >> sourceShift;

        masked = instruction & destinationMask;
        destinationRegister = masked >> destShift;

        masked = instruction & AbstractInstruction.LOWER_16_IMMEDIATE_MASK;
        immediate = masked;
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("executing instruction type : " + descriptor + " \n");
        toReturn.append("source register is : " + sourceRegister + "\n");
        toReturn.append("destination register is : " + destinationRegister + "\n");
        toReturn.append("immediate value is  : 0x" + Integer.toHexString(immediate).toUpperCase() + "\n");
        return toReturn.toString();
    }

    @Override
    public int assembleInstruction(String instruction) {
        int toReturn = 0;
        String[] split = instruction.split(" ");
        int rSource = super.getIntForRegisterName(split[1]);
        int rDestination = super.getIntForRegisterName(split[2]);
        int imm = parseImmediate(split[3]);
        toReturn |= opcode << AbstractInstruction.OP_CODE_SHIFT;
        toReturn |= rSource << sourceShift;
        toReturn |= rDestination << destShift;
        toReturn |= imm;
        return toReturn;
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
