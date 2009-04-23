/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.ALUImmediateInstructions;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractALUImmediateInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *This class will or a register with an immediate value. It takes instructions
 * of the form: " ori Rs Rd 0xIMMM .where immediate is a 16 bit value
 * which will be zero-extended to a 32 bit value.
 * @author amit
 */
public class OrImmediateInstruction extends AbstractALUImmediateInstruction {

    static final int OR_IMM_OPCODE = 0x0D;
    static final String OR_OPERATION_STRING = "or operation";
    public OrImmediateInstruction() {
        super(OR_IMM_OPCODE,OR_OPERATION_STRING);
    }

    public OrImmediateInstruction(int instruction) {
        this();
        setRegisters(instruction);
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new OrImmediateInstruction(instruction);
    }

    public void executeOperation(RegisterFile reg) {
        int sourceValue = reg.getValueForRegister(super.sourceRegister);
        int result = sourceValue | immediate;
        reg.setValueForRegister(destinationRegister, result);
    }

    public static void main(String[] args) {
       String instruction = "ori r16 r6 L0x1bcdF678";
       OrImmediateInstruction o = new OrImmediateInstruction();
       int newInstr = o.assembleInstruction(instruction);
       System.out.println("parsed instruction is " + o.zeroPadIntForString(newInstr, 32));
       System.out.println("\n\n and dissasembled : \n"  + o.getAbstractInstruction(newInstr));
    }
}
