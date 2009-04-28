/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.BranchInstructions;

import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.BranchInstructionInterface;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *This instruction is a branch if equal instruction
 * which is of the form: 'beq r1 r2 #label'
 * If r1 == r2 then we set the pc to #label
 * @author amit
 */
public class JumpAndLinkInstruction extends AbstractInstruction implements BranchInstructionInterface {

    static final int opcode = 0x02;
    static final String description = "jump and link";
    static final int LABEL_MASK = 0x3FFFFFF;
    InstructionTypes type = InstructionTypes.branch;
    //instruction variables
    int label;

    public JumpAndLinkInstruction() {
        super();
    }

    public JumpAndLinkInstruction(int instruction) {
        this();
        setRegisters(instruction);
    }

    public void setRegisters(int instruction) {
        label = instruction & LABEL_MASK;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public void setPC(RegisterFile reg) {
        reg.setRetReg(reg.getPC() + (8 * Globals.WORD_SIZE));
        reg.setPC(label);
    }

    public static void main(String[] args) {
        String instruction = "jal 0x1234";
        JumpAndLinkInstruction j = new JumpAndLinkInstruction();
        int instr = j.assembleInstruction(instruction);
        System.out.println(j.getAbstractInstruction(instr).toString());

    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("instruction type: Jump and Link\n");
        toReturn.append("jump to location: " + Integer.toHexString(label));
        return toReturn.toString();
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new JumpAndLinkInstruction(instruction);
    }

    @Override
    public int assembleInstruction(String instruction) {
        String[] split = instruction.split(" ");
        int toReturn = 0;
        long decoded = Long.decode(split[1]);
        decoded = decoded & 0xFFFFFFFF;
        int intImm = ((int) decoded) & LABEL_MASK;
        toReturn |= opcode << OP_CODE_SHIFT;
        toReturn |= intImm;
        return toReturn;
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }
}
