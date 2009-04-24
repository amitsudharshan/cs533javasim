/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.abstractandinterface;

import org.cs533.newprocessor.assembler.instructionTypes.BranchInstructions.BranchIfEqualInstruction;

/**
 *
 * @author amit
 */
public abstract class AbstractBranchInstruction extends AbstractInstruction implements BranchInstructionInterface {

    public int registerOne;
    public int registerTwo;
    public int label;
    InstructionTypes type = InstructionTypes.branch;

    /*decoding masks */
    static final int regOneMask = 0x03E00000;
    static final int regTwoMask = 0x001F0000;

    /* Shift bits */
    static final int regOneShift = 21;
    static final int regTwoShift = 16;
    int opcode;

    public AbstractBranchInstruction(int opCode) {
        opcode = opCode;
    }

    public AbstractBranchInstruction(int instruction, int opCode) {
        this(opCode);
        setRegisters(instruction);
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("executing instruction: branch if equal\n");
        toReturn.append(" with register one as #" + registerOne + "\n");
        toReturn.append(" and with register two as #" + registerTwo + "\n");
        toReturn.append(" branching to 0x" + Integer.toHexString(label) + "\n");
        return toReturn.toString();
    }

    public void setRegisters(int instruction) {
        int masked = instruction & regOneMask;
        registerOne = masked >> regOneShift;

        masked = instruction & regTwoMask;
        registerTwo = masked >> regTwoShift;

        masked = instruction & AbstractInstruction.LOWER_16_IMMEDIATE_MASK;
        label = masked;
    }

    @Override
    public int assembleInstruction(String instruction) {
        int toReturn = 0;
        String[] split = instruction.split(" ");
        int regOne = getIntForRegisterName(split[1]);
        int labelIndex = 2;
        int regTwo = 0;
        if (split.length == 4) {
            //if branch has two regesters (only useful for beq)
            regTwo = getIntForRegisterName(split[2]);
            labelIndex = 3;
        }
        int lbl = parseImmediate(split[labelIndex]);
        toReturn |= opcode << AbstractInstruction.OP_CODE_SHIFT;
        toReturn |= regOne << regOneShift;
        toReturn |= regTwo << regTwoShift;
        toReturn |= lbl;
        return toReturn;
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }
}
