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

    public int getValueForRegister(int regNumber) {
        return registerFile[regNumber];
    }

    public void setValueForRegister(int regNumber, int value) {
        registerFile[regNumber] = value;
    }
}
