/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *This compare and swap instruction looks like
 * ' cas r1 r2 r3 'which puts r3 into m[r1] if m[r1] == r2
 * It then puts into the swap register the value of m[r1] before the swap
 * @author amit
 */
public class CompareAndSwapInstruction extends AbstractInstruction implements MemoryInstructionInterface {

    int opcode = 0x3E;
    int addressRegister;
    int compareRegister;
    int swapRegister;

    /*decoding masks */
    static final int addressMask = 0x03E00000;
    static final int compareMask = 0x001F0000;
    static final int swapMask = 0x0000F800;
    /* Shift bits */
    static final int addressShift = 21;
    static final int compareShift = 16;
    static final int swapShift = 11;
    static final InstructionTypes type = InstructionTypes.memory;

    public CompareAndSwapInstruction() {
    }

    public CompareAndSwapInstruction(int instruction) {
        this();
        System.out.println("Creating CAS!");
        setRegisters(instruction);
    }

    public void setRegisters(int instruction) {
        int masked = instruction & addressMask;
        addressRegister = masked >> addressShift;

        masked = instruction & compareMask;
        compareRegister = masked >> compareShift;

        masked = instruction & swapMask;
        swapRegister = masked >> swapShift;
    }

    @Override
    public int assembleInstruction(String instruction) {
        System.out.println("ASSEMBLING CAS INSTRUCTION!");
        int toReturn = -1;
        int instr = 0;
        String[] split = instruction.split(" ");
        instr |= opcode << AbstractInstruction.OP_CODE_SHIFT;
        instr |= getIntForRegisterName(split[1]) << addressShift;
        instr |= getIntForRegisterName(split[2]) << compareShift;
        instr |= getIntForRegisterName(split[3]) << swapShift;
        toReturn = instr;
        return toReturn;

    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new CompareAndSwapInstruction(instruction);
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    public MemoryInstruction getMemoryInstruction(RegisterFile rFile) {
        byte[] compareValue = intToByteArray(rFile.getValueForRegister(compareRegister));
        int address = rFile.getValueForRegister(addressRegister);
        byte[] swapValue = intToByteArray(rFile.getValueForRegister(swapRegister));
        return MemoryInstruction.CompareAndSwap(address, compareValue, swapValue);
    }

    public void handleWriteBack(byte[] toWriteBack, RegisterFile rFile) {
        int gotBack = byteArrayToInt(toWriteBack);
        rFile.setValueForRegister(swapRegister, gotBack);
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("Executing instruction: Compare and Swap\n");
        toReturn.append("address register is : #" + addressRegister + "\n");
        toReturn.append("compare register is :#" + compareRegister + "\n");
        toReturn.append("swap register is : #" + swapRegister + "\n");

        return toReturn.toString();
    }

    public static void main(String[] args) {
        String instr = "cas r1 r2 r3";
        CompareAndSwapInstruction c = new CompareAndSwapInstruction();
        int instruction = c.assembleInstruction(instr);
        AbstractInstruction.getAbstractInstructionForInstruction(instruction).toString();
        c = (CompareAndSwapInstruction) c.getAbstractInstruction(instruction);
        System.out.println(c);
        System.out.println(c.zeroPadIntForString(instruction, 32));


    }
}
