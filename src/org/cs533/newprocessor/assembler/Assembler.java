/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.instructionTypes.ALUInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.StoreInstruction;

/**
 *
 * @author amit
 */
public class Assembler {

    public enum Sections {

        start, data, instructions
    }
    Sections currentSection = Sections.start;
    int nextAddress = 0;
    HashMap<String, Integer> variableToAddressMap = new HashMap<String, Integer>();
    HashMap<String, Integer> labelToAddressMap = new HashMap<String, Integer>();
    HashMap<String, AbstractInstruction> opCodeToAssemblerMap = new HashMap<String, AbstractInstruction>();
    ArrayList<Integer> memoryImage = new ArrayList<Integer>();

    public Assembler() {
        populateOpCodeAssemblerMap();
    }

    public void populateOpCodeAssemblerMap() {
        opCodeToAssemblerMap.put("add", new ALUInstruction());
        opCodeToAssemblerMap.put("store",new StoreInstruction());
    }

    public static void main(String[] args) throws Exception {
        Assembler a = new Assembler();
        String fileName = "/home/amit/asm/test.asm";
        String[] split = "add r3 r3 r2".split(" ");
        for(int i =0; i<split.length;i++ ) {
            System.out.println(i + " : " + split[i]);
        }
    }

    public void processLine(String line) {
        String[] split = line.split(" ");
        if (split[0].equals("")) {
            return;
        }
        switch (currentSection) {
            case start:
                if (split[0].equals("#data")) {
                    currentSection = Sections.data;
                } else {
                    throw new java.lang.RuntimeException("ERROR FILE MUST START WITH data section");
                }
                break;
            case data:
                if (split[0].equals("#instructions")) {
                    currentSection = Sections.data;
                } else if (split[0].startsWith("$")) {
                    variableToAddressMap.put(split[0], nextAddress);
                    nextAddress += Globals.WORD_SIZE * 8;
                    int value = Integer.decode(split[1]);
                    memoryImage.add(value);
                } else {
                    throw new java.lang.RuntimeException("in data state found non matching instruction");
                }
                break;
            case instructions:
                if (split[0].startsWith("#")) {
                    labelToAddressMap.put(split[0], nextAddress);
                } else {
                    int instruction = parseInstruction(line);
                    if (instruction == -1) {
                        throw new java.lang.RuntimeException("found an invalid instruction");
                    } else {
                        memoryImage.add(instruction);
                        nextAddress += Globals.WORD_SIZE * 8;
                    }
                }
        }
    }

    public int parseInstruction(String instruction) {
        int toReturn = -1;
        String[] split = instruction.split(" ");
        //NOW WE ITERATE THROUGH THE SPLIT AND REPLACE LABELS WITH ADDRESSES
        for (int i = 0; i < split.length; i++) {
            Integer variable = variableToAddressMap.get(split[i]);
            Integer label = labelToAddressMap.get(split[i]);
            if (variable != null) {
                split[i] = "$" + variable;
            } else if (label != null) {
                split[i] = "#" + label;
            }
        }
        // now we pass this along to the assembler
        AbstractInstruction assembler = opCodeToAssemblerMap.get(split[0]);
        if (assembler != null) {
            toReturn = assembler.assembleInstruction(instruction);
        }
        return toReturn;
    }

    public ArrayList<String> getAndStoreLines(String file) throws IOException {
        BufferedReader bRead = new BufferedReader(new FileReader(file));
        ArrayList<String> tokens = new ArrayList<String>();
        String line = null;
        while ((line = bRead.readLine()) != null) {
            if (line.compareTo("") != 0) {
                tokens.add(line);
            }
        }
        return tokens;
    }
}
