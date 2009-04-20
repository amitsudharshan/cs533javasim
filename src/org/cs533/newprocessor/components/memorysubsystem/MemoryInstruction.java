/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.Globals;

/**
 *
 * @author amit
 */
public class MemoryInstruction {

    int inAddress;
    byte[] inData;
    boolean isWriteInstruction;
    boolean isCompleted;

    /* output ports */
    byte[] outData;

    public MemoryInstruction(int inAddress, byte[] data, boolean isWriteInstruction) {
        this.inAddress = inAddress;
        this.inData = data;
        this.isWriteInstruction = isWriteInstruction;
        this.isCompleted = false;
    }

    public static void main(String[] args) {
        int add = (5/Globals.CACHE_LINE_SIZE) * Globals.CACHE_LINE_SIZE;
        System.out.println(add);
    }

    public byte[] getInData() {
        return inData;
    }

    public byte[] getOutData() {
        return outData;
    }

    public void setOutData(byte[] out) {
        outData = out;
    }

    public void setIsCompleted(boolean is) {
        isCompleted = is;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public int getInAddress() {
        return inAddress;
    }

    public boolean isIsWriteInstruction() {
        return isWriteInstruction;
    }
}
