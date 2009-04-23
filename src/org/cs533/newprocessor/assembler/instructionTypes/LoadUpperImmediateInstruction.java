/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes;

import org.cs533.newprocessor.assembler.abstractandinterface.ALUInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 * This class implemements a load upper immediate instruction lui which takes the form.
 * lui Rd imm
 * takes a 16bit immediate value and loads it into the upper 16 bits
 * of the destination register
 * @author amit
 */
public class LoadUpperImmediateInstruction extends AbstractInstruction implements ALUInstructionInterface {

    public static final InstructionTypes type = InstructionTypes.alu;
    public static final int opcode = 0x0F;

    /* masks and shift */
    static int destMask = 0x001F0000;
    static int destShift = 16;
    int registerDestination;
    int immediate;

    public LoadUpperImmediateInstruction() {
    }

    public LoadUpperImmediateInstruction(int instruction) {
        this();
        setRegisters(instruction);

    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("Executing instruction type LoadUpperImmediate\n");
        toReturn.append("destination register is " + registerDestination + "\n");
        toReturn.append(" immediate value is " + Integer.toHexString(immediate));
        return toReturn.toString();
    }

    public void setRegisters(int instruction) {
        int mask = instruction & destMask;
        registerDestination = mask >> destShift;
        mask = instruction & LOWER_16_IMMEDIATE_MASK;
        immediate = mask;
    }

    @Override
    public int assembleInstruction(String instruction) {
        int toReturn = 0;
        String[] split = instruction.split(" ");
        int destReg = getIntForRegisterName(split[1]);
        System.out.println("IMMEDIATE IS " + split[2]);
        int imm = parseImmediate(split[2]);
        toReturn |= opcode << OP_CODE_SHIFT;
        toReturn |= destReg << destShift;
        toReturn |= imm;
        return toReturn;
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new LoadUpperImmediateInstruction(instruction);
    }

    public void executeOperation(RegisterFile reg) {
        int oldValue = reg.getValueForRegister(registerDestination);
        int toOr = immediate << 16;
        int newValue = toOr | oldValue;
        reg.setValueForRegister(registerDestination, newValue);
    }

    public static void main(String[] args) {
        String strInstr = "lui r1 U0x32";
        LoadUpperImmediateInstruction l = new LoadUpperImmediateInstruction();
        int instruction = l.assembleInstruction(strInstr);
        System.out.println(l.zeroPadIntForString(instruction, 32));
        l = new LoadUpperImmediateInstruction(instruction);
        System.out.println(l);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }
}
