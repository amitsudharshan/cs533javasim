/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.abstractandinterface.MemoryInstructionInterface;
import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.SubTypes;

/**
 * This class contains an invalidate store instruction implemenation
 * So far this instruction will only support store from register source into
 * register destiation.
 * It takes instructions of the form: ivsw r1 r2 which will place the
 * contents of r2 in the memory location at address r1.
 * @author amit
 */
public class InvalidateStoreWordInstruction extends AbstractInstruction implements MemoryInstructionInterface {

    static final int opcode = 0x3C;
    static final InstructionTypes type = InstructionTypes.memory;
    static final int addressRegMask = 0x03E00000;
    static final int contentRegMask = 0x001F0000;
    static final int addressRegShift = 21;
    static final int contentRegShift = 16;
    /* The values needed by the instruction */
    int registerContent;
    int registerAddress;
    int offset;

    public InvalidateStoreWordInstruction() {
    }

    public InvalidateStoreWordInstruction(int instruction) {
        this();
        setRegisters(instruction);
    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new InvalidateStoreWordInstruction(instruction);
    }

    public void setRegisters(int instruction) {
        registerAddress = (instruction & addressRegMask) >> addressRegShift;
        registerContent = (instruction & contentRegMask) >> contentRegShift;
    }

    public static void main(String[] args) {
        InvalidateStoreWordInstruction store = new InvalidateStoreWordInstruction();
        int instr = store.assembleInstruction("store r2 r3");
        System.out.println("THE INSTRUCTION IS : " + store.zeroPadIntForString(instr, 32));
        store = new InvalidateStoreWordInstruction(instr);
        System.out.println(store);

    }

    @Override
    public String toString() {
        return "This is an invalidate store instruction \n with " +
                "registerAddress = " + registerAddress + " " +
                "and registerContent = " + registerContent;
    }

    @Override
    public int assembleInstruction(String instruction) {
        //instruction = "store r1 r2" places M[r1] = r2;
        int instr = 0;
        String[] tokens = instruction.split(" ");
        int reg1 = getIntForRegisterName(tokens[1]);
        int reg2 = getIntForRegisterName(tokens[2]);

        if (tokens.length > 3) {
            offset = signExtendSixteenBitInt(parseImmediate(tokens[3]));

        } else {
            offset = 0;
        }
        instr = opcode << AbstractInstruction.OP_CODE_SHIFT;
        instr |= reg1 << addressRegShift;
        instr |= reg2 << contentRegShift;
        instr |= offset;
        return instr;
    }

    /**
     * This method will return a memoryinstruction which contains the address
     * and value to store.
     * @param rFile The registerfile
     * @return the memory instruction to send to memory
     */
    public MemoryInstruction getMemoryInstruction(RegisterFile rFile) {
        int address = rFile.getValueForRegister(registerAddress);
        byte[] toStore = intToByteArray(rFile.getValueForRegister(registerContent));
        MemoryInstruction store = MemoryInstruction.Store(address, toStore);
        store.setSubType(SubTypes.InvalidateStore);
        return store;
    }

    public void handleWriteBack(byte[] toWriteBack, RegisterFile rFile) {
        return;
    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }
}
