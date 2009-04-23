/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.instructionTypes;

import java.util.HashMap;
import org.cs533.newprocessor.assembler.abstractandinterface.ALUInstructionInterface;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.core.RegisterFile;

/**
 *This is a representation of a three parameter ALU Instruction
 *This instruction takes the form " add Rd,Rs,Rt " and executes
 * Rd = Rs+Rt. This is only for the case of add.
 * We will also do sub, and mul eventually
 * @author amit
 */
public class ThreeRegisterALUInstruction extends AbstractInstruction implements ALUInstructionInterface {

    int registerSource1;
    int registerSource2;
    int registerDestination;
    int functionCode;

    /* opcode */
    static final int opcode = 0;
    static final InstructionTypes type = InstructionTypes.alu;

    /*decoding masks */
    static final int source1Mask = 0x03E00000;
    static final int source2Mask = 0x001F0000;
    static final int destMask = 0x0000F800;
    static final int functionCodeMask = 0x0000003F;
    /* Shift bits */
    static final int source1Shift = 21;
    static final int source2Shift = 16;
    static final int destShift = 11;
    public HashMap<Integer, String> aluFunctionCodes;
    public HashMap<String, Integer> functionCodeForInstruction;

    public ThreeRegisterALUInstruction() {
        //This constructor is only called once in the Assembler to be able to
        // get a reference to the dissasemble method
        createFunctionCodesMap();
    }

    public ThreeRegisterALUInstruction(int instruction) {
        this();
        setRegisters(instruction);

    }

    @Override
    public AbstractInstruction getAbstractInstruction(int instruction) {
        return new ThreeRegisterALUInstruction(instruction);
    }

    public void createFunctionCodesMap() {
        aluFunctionCodes = new HashMap<Integer, String>();
        functionCodeForInstruction = new HashMap<String, Integer>();
        aluFunctionCodes.put(0x20, "add");
        functionCodeForInstruction.put("add", 0x20);
    }

    public void setRegisters(int instruction) {
        int masked = instruction & source1Mask;
        registerSource1 = masked >> source1Shift;

        masked = instruction & source2Mask;
        registerSource2 = masked >> source2Shift;

        masked = instruction & destMask;
        registerDestination = masked >> destShift;

        masked = instruction & functionCodeMask;
        functionCode = masked;
        System.out.println("got function code = " + functionCode);
    }

    @Override
    public int assembleInstruction(String instruction) {
        int toReturn = -1;
        int instr = 0;
        String[] split = instruction.split(" ");
        instr |= opcode << AbstractInstruction.OP_CODE_SHIFT;
        instr |= getIntForRegisterName(split[1]) << source1Shift;
        instr |= getIntForRegisterName(split[2]) << source2Shift;
        instr |= getIntForRegisterName(split[3]) << destShift;
        Integer theFunction = functionCodeForInstruction.get(split[0]);
        if (theFunction != null) {
            instr |= theFunction;
            toReturn = instr;
        }
        return toReturn;
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("instruction functionCode : " + functionCode + "\n");
        toReturn.append("\t functionCode type : " + aluFunctionCodes.get(functionCode) + "\n");
        toReturn.append("source 1 register is : " + registerSource1 + "\n");
        toReturn.append("source 2 register is : " + registerSource2 + "\n");
        toReturn.append("destination register is : " + registerDestination + "\n");
        return toReturn.toString();

    }

    public static void main(String[] args) {
        ThreeRegisterALUInstruction a = new ThreeRegisterALUInstruction();
        String instruction = "add r28 r3 r16 ";
        int instructionBin = a.assembleInstruction(instruction);
        System.out.println("int value is " + Integer.toHexString(instructionBin));
        System.out.println(a.zeroPadIntForString(instructionBin, 32));
        ThreeRegisterALUInstruction aF = new ThreeRegisterALUInstruction(instructionBin);
        System.out.println(aF);

    }

    @Override
    public InstructionTypes getType() {
        return type;
    }

    public void executeOperation(RegisterFile reg) {
        if (aluFunctionCodes.get(functionCode).equals("add")) {
            int op1 = reg.getValueForRegister(registerSource1);
            int op2 = reg.getValueForRegister(registerSource2);
            reg.setValueForRegister(registerDestination, op1 + op2);
        }
    }

    @Override
    public int getOpcode() {
        return opcode;
    }
}
