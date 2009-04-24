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
 *Takes form blez r1 label
 * branches to label if r1 < 0
 * @author amit
 */
public class BranchIfLessThanOrEqualToZero extends AbstractBranchInstruction {

    static final int opcode = 0x06;

    public BranchIfLessThanOrEqualToZero() {
        super(opcode);
    }

    public BranchIfLessThanOrEqualToZero(int instruction) {
        super(instruction,opcode);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public void setPC(RegisterFile reg) {
        int reg1 = reg.getValueForRegister(registerOne);
        if (reg1 <= 0) {        
            reg.setPC(label);
        } else {
            reg.incrementPC(Globals.WORD_SIZE * 8);
        }
    }

    public static void main(String[] args) {
        String instruction = "bleq r3 r4 0xF234";
        BranchIfEqualInstruction b = new BranchIfEqualInstruction();
        int instr = b.assembleInstruction(instruction);
        System.out.println(b.getAbstractInstruction(instr).toString());

    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new BranchIfLessThanOrEqualToZero(instruction);
    }
}


