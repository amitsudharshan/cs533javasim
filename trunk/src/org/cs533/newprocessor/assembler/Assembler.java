/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import org.cs533.newprocessor.Globals;

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
    HashMap<String,BitSet> opCodeMap = new HashMap<String,BitSet>();
    ArrayList<BitSet> memoryImage = new ArrayList<BitSet>();

    public Assembler() {
        loadOpCodeMap();
    }

    public void loadOpCodeMap() {
        //DO SOMETHING HERE!
    }
    public static void main(String[] args) throws Exception {
        Assembler a = new Assembler();
        String fileName = "/home/amit/asm/test.asm";
        System.out.println(a.getTokens(fileName));
    }

    public void processToken(String token) {
        String[] split = token.split(" ");
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
                    BitSet inBin = getBitSetForInteger(value);
                    memoryImage.add(inBin);
                } else {
                    throw new java.lang.RuntimeException("in data state found non matching instruction");
                }
                break;
            case instructions:
                if (split[0].startsWith("#")) {
                    labelToAddressMap.put(split[0], nextAddress);
                } else {
                    BitSet instruction = parseInstruction(split);
                    if (instruction == null) {
                        throw new java.lang.RuntimeException("found an invalid instruction");
                    } else {
                        memoryImage.add(instruction);
                        nextAddress += Globals.WORD_SIZE * 8;
                    }
                }

        }
    }

    public BitSet parseInstruction(String[] instruction) {
        BitSet toReturn = null;
        BitSet opCode = opCodeMap.get(instruction[0]);
        if(opCode!=null) {

        }
        return toReturn;
    }

    public BitSet getBitSetForInteger(int value) {
        BitSet toReturn = new BitSet(32);
        String bits = Integer.toBinaryString(value);
        int counter = 0;
        for (int i = bits.length() - 1; i >= 0; i--) {
            if (bits.charAt(i) == '1') {
                toReturn.set(counter);
            }
            counter++;
        }
        return toReturn;
    }

    public ArrayList<String> getTokens(String file) throws IOException {
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
