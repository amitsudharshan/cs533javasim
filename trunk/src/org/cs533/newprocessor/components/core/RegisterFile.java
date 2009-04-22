/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.core;

/**
 *
 * @author amit
 */
public class RegisterFile {

    public static final int NUM_REGISTERS = 32; // THIS IS DETERMINED BY PROCESSOR ISA
    public int[] registerFile = new int[NUM_REGISTERS];
    int pc = -1;

    public int getValueForRegister(int regNumber) {
        return registerFile[regNumber];
    }

    public void setValueForRegister(int regNumber, int value) {
        registerFile[regNumber] = value;
    }

    public void setPC(int pc_) {
        pc = pc_;
    }

    public int getPC() {
        return pc;
    }

    public void incrementPC(int offset) {
        pc += offset;
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        toReturn.append("---------REGISTER FILE ---------------\n");
        toReturn.append("pc: 0x" + Integer.toHexString(pc)+"\n");
        for(int i =0; i < registerFile.length;i++) {
            toReturn.append("r"+i+" : 0x"+Integer.toHexString(registerFile[i])+"\n");
        }
        toReturn.append("--------------------------------------\n");
        return toReturn.toString();
    }
}
