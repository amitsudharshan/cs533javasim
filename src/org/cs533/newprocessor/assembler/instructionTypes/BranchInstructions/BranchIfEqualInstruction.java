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
 *This instruction is a branch if equal instruction
 * which is of the form: 'beq r1 r2 #label'
 * If r1 == r2 then we set the pc to #label
 * @author amit
 */
public class BranchIfEqualInstruction extends AbstractBranchInstruction {

    static final int opcode = 0x04;

    public BranchIfEqualInstruction() {
        super(opcode);
    }
    public  BranchIfEqualInstruction(int instruction) {
        super(instruction,opcode);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public void setPC(RegisterFile reg) {
        int reg1 = reg.getValueForRegister(registerOne);
        int reg2 = reg.getValueForRegister(registerTwo);
        if (reg1 == reg2) {
            reg.setPC(label);
        } else {
            reg.incrementPC(Globals.WORD_SIZE * 8);
        }
    }

    public static void main(String[] args) {
        String instruction = "beq r3 r4 0xF234";
        BranchIfEqualInstruction b = new BranchIfEqualInstruction();
        int instr = b.assembleInstruction(instruction);
        System.out.println(b.getAbstractInstruction(instr).toString());

    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new BranchIfEqualInstruction(instruction);
    }
}
