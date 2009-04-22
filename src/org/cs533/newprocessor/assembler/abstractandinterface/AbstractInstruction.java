/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.abstractandinterface;

import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

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

    public abstract int assembleInstruction(String instruction);

    public abstract InstructionTypes getType();

    public static int getIntForRegisterName(String reg) {
        int regNumber = Integer.parseInt(reg.replaceAll("r", ""));
        return regNumber;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value};
    }

    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
    }

    public static String zeroPadIntForString(int value, int totalBits) {
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
        System.out.println("AbstractInstruction");
    }
}
