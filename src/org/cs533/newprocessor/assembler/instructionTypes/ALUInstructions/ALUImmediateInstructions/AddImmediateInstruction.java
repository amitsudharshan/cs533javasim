/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.ALUInstructions.ALUImmediateInstructions;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractALUImmediateInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *addi r1 0x1
 * @author amit
 */
public class AddImmediateInstruction extends AbstractALUImmediateInstruction {

    static final int ADD_IMM_OPCODE = 0x08;
    static final String ADD_OPERATION_STRING = "add imm operation";

    public AddImmediateInstruction() {
        super(ADD_IMM_OPCODE, ADD_OPERATION_STRING);
    }

    public AddImmediateInstruction(int instruction) {
        this();
        setRegisters(instruction);
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new AddImmediateInstruction(instruction);
    }

    public void executeOperation(RegisterFile reg) {
        int sum = reg.getValueForRegister(sourceRegister) + immediate;
        reg.setValueForRegister(destinationRegister, sum);
    }
}
