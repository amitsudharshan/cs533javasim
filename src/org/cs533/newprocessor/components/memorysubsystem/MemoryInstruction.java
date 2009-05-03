/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;

/**
 *
 * @author amit
 */
public class MemoryInstruction implements Cloneable {

    public enum InstructionType {

        Load, Store, CAS
    }
    InstructionType type;
    int type_flags;
    int inAddress;
    byte[] inData;
    byte[] compareData;
    boolean isCompleted;

    /* output ports */
    byte[] outData;

    public static MemoryInstruction Load(int inAddress) {
        return new MemoryInstruction(inAddress, null, null, InstructionType.Load);
    }

    public static MemoryInstruction Store(int inAddress, byte[] data) {
        return new MemoryInstruction(inAddress, null, data, InstructionType.Store);
    }

    public static MemoryInstruction CompareAndSwap(int inAddress, byte[] compareData, byte[] swapData) {
        return new MemoryInstruction(inAddress, compareData, swapData, InstructionType.CAS);
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer();
        int dataIn = -1;
        int dataOut = -1;
        int compareIn = -1;

        String dataToReturn = "";
        if (inData != null) {
            dataIn = AbstractInstruction.byteArrayToInt(inData);
            dataToReturn += " inData is 0x" + Integer.toHexString(dataIn) + " ";
        } else {
            dataToReturn += " inData is null ";
        }
        if (outData != null) {
            dataOut = AbstractInstruction.byteArrayToInt(outData);
            dataToReturn += " outData is 0x" + Integer.toHexString(dataOut) + " ";
        } else {
            dataToReturn += " outData is  null ";
        }
        if (compareData != null) {
            compareIn = AbstractInstruction.byteArrayToInt(compareData);
            dataToReturn += " compareData is 0x" + Integer.toHexString(compareIn);
        } else {
            dataToReturn += " compareData is null";
        }

        toReturn.append("instruction type: " + type.toString() + " address " + inAddress + " data is: " + dataToReturn);

        return toReturn.toString();
    }

    protected MemoryInstruction(int inAddress, byte[] compareData, byte[] data, InstructionType type) {
        this.type = type;
        this.type_flags = 0;
        this.inAddress = inAddress;
        this.inData = data;
        this.compareData = compareData;
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

    @Override
    public MemoryInstruction clone() {
        try {
            return (MemoryInstruction) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(MemoryInstruction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
