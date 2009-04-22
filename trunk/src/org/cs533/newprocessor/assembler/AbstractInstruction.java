/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import org.cs533.newprocessor.assembler.instructionTypes.ALUInstruction;

/**
 *
 * @author amit
 */
public abstract class AbstractInstruction {

    public static final int OP_CODE_MASK = 0xF9000000;
    public static final int OP_CODE_SHIFT = 26;

    public enum InstructionTypes {

        alu, branch, memory
    }
    public abstract int dissasembleInstruction(String instruction);

    public int getIntForRegisterName(String reg) {
        int regNumber = Integer.parseInt(reg.replaceAll("r", ""));
        return regNumber;
    }

    public String zeroPadIntForString(int value, int totalBits) {
        String bin = Integer.toBinaryString(value);
        while (bin.length() != totalBits) {
            bin = "0" + bin;
        }
        int size = bin.length();
        StringBuffer buff = new StringBuffer(bin);
        int counter = -3;
        for (int i = size - 1; i >= 0; i--) {
            if (counter++ % 4 == 0) {
                buff.insert(i, " ");
            }
        }
        return buff.toString().trim();
    }

    public static void main(String[] args) {
        ALUInstruction.main(null);
    }
}
