/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.BranchInstructions;

import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractBranchInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *Takes form bgez r1 label
 * branches to label if r1 < 0
 * @author amit
 */
public class BranchIfGreaterThanOrEqualToZero extends AbstractBranchInstruction {

    static final int opcode = 0x01;
    static final String description = " branch if greater than or equal to zero";

    public BranchIfGreaterThanOrEqualToZero() {
        super(opcode, description);
    }

    public BranchIfGreaterThanOrEqualToZero(int instruction) {
        super(instruction, opcode,description);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public void setPC(RegisterFile reg) {
        int reg1 = reg.getValueForRegister(registerOne);
        if (reg1 >= 0) {
            reg.setPC(label);
        } else {
            reg.incrementPC(Globals.WORD_SIZE );
        }
    }

    public static void main(String[] args) {
        String instruction = "bgez r3 r4 0xF234";
        BranchIfEqualInstruction b = new BranchIfEqualInstruction();
        int instr = b.assembleInstruction(instruction);
        System.out.println(b.getAbstractInstruction(instr).toString());

    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new BranchIfGreaterThanOrEqualToZero(instruction);
    }
}


