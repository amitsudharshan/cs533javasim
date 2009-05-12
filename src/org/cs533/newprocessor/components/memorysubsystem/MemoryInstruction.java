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

    public enum SubTypes {

        None, UpdateStore, InvalidateStore
    }
    InstructionType type;
    SubTypes subType;
    int inAddress;
    byte[] inData;
    byte[] compareData;
    boolean isCompleted;

    /* output ports */
    byte[] outData;


    public SubTypes getSubType() {
        return subType;
    }

    public void setSubType(SubTypes subType) {
        this.subType = subType;
    }
    public static MemoryInstruction Load(int inAddress) {
        return new MemoryInstruction(inAddress, null, null, InstructionType.Load);
    }

    public static MemoryInstruction Store(int inAddress, byte[] data) {
        assert inAddress >= 0;
        return new MemoryInstruction(inAddress, null, data, InstructionType.Store);
    }

    public static MemoryInstruction CompareAndSwap(int inAddress, byte[] compareData, byte[] swapData) {
        return new MemoryInstruction(inAddress, compareData, swapData, InstructionType.CAS);
    }

    @Override
    public String toString() {
        switch (type) {
            case CAS:
                return "CAS(0x"+Integer.toHexString(inAddress)
                        +",0x"+Integer.toHexString(AbstractInstruction.byteArrayToInt(compareData))
                        +"->0x"+Integer.toHexString(AbstractInstruction.byteArrayToInt(inData))
                        +")"+(this.isCompleted ? "=>0x"+Integer.toHexString(AbstractInstruction.byteArrayToInt(outData)) : "");
            case Load:
                return "Load(0x"+Integer.toHexString(inAddress)
                        +")"+(this.isCompleted ? "=>0x"+Integer.toHexString(AbstractInstruction.byteArrayToInt(outData)) : "");
            case Store:
                return "Store(0x"+Integer.toHexString(inAddress)
                        +"->0x"+Integer.toHexString(AbstractInstruction.byteArrayToInt(inData))
                        +")"+(this.isCompleted ? "--done" : "");
            default:
                throw new RuntimeException("Unknown MemoryInstruction type "+type.toString());
        }
    }
    public String oldToString() {
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
        this.subType = SubTypes.None;
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

    public byte[] getCompareData() {
        return compareData;
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

    public SubTypes getTypeFlags() {
        return subType;
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
