/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;

/**
 *
 * @author amit
 */
public class HaltInstruction extends AbstractInstruction {

    public InstructionTypes type = InstructionTypes.halt;
    static final int opcode = 0x3F;

    public HaltInstruction() {
    }

    public HaltInstruction(int instruction) {
    }

    @Override
    public int assembleInstruction(String instruction) {
        int toReturn = 0;
        toReturn = opcode << OP_CODE_SHIFT;
        return toReturn;
    }

    @Override
    public String toString() {
        return "instruction type: HALT";
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new HaltInstruction(instruction);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }
}
