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

    public enum InstructionType {

        Load, Store, CAS
    }
    InstructionType type;
    int type_flags;
    int inAddress;
    byte[] inData;
    boolean isCompleted;

    /* output ports */
    byte[] outData;

    public static MemoryInstruction Load(int inAddress) {
        return new MemoryInstruction(inAddress, null, InstructionType.Load);
    }

    public static MemoryInstruction Store(int inAddress, byte[] data) {
        return new MemoryInstruction(inAddress, data, InstructionType.Store);
    }
    
    public static MemoryInstruction CompareAndSwap(int inAddress, byte[] data) {
        return new MemoryInstruction(inAddress,data,InstructionType.CAS);
    }

    protected MemoryInstruction(int inAddress, byte[] data, InstructionType type) {
        this.type = type;
        this.type_flags = 0;
        this.inAddress = inAddress;
        this.inData = data;
        this.outData = null;
        this.isCompleted = false;
    }

    public static void main(String[] args) {
        int add = (5 / Globals.CACHE_LINE_SIZE) * Globals.CACHE_LINE_SIZE;
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

    public InstructionType getType() {
        return type;
    }
    public int getTypeFlags() {
        return type_flags;
    }
}
