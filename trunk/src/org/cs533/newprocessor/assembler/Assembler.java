/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.simulator.ExecutableImage;

/**
 *
 * @author amit
 */
public class Assembler {

    public enum Sections {

        start, data, instructions, pcinit
    }
    Sections currentSection = Sections.start;
    int nextAddress = 0;
    HashMap<String, Integer> variableToAddressMap = new HashMap<String, Integer>();
    HashMap<String, Integer> labelToAddressMap = new HashMap<String, Integer>();
    HashMap<String, AbstractInstruction> opCodeToAssemblerMap;
    ArrayList<Integer> memoryImage = new ArrayList<Integer>();
    ArrayList<Integer> pcStart = new ArrayList<Integer>();

    public Assembler() {
        opCodeToAssemblerMap = OpcodeMetaData.populateOpCodeAssemblerMap();
    }

    public static void main(String[] args) throws Exception {
        String inputFile = "/home/amit/asm/test.asm";
        String outputFile = "/home/amit/asm/a.out";
        if (args.length >= 2) {
            inputFile = args[0];
            outputFile = args[1];
        }
        assembleFile(inputFile, outputFile);
    }

    public static void assembleFile(String inputFile, String outputFile) {
        try {
            Assembler a = new Assembler();
            ArrayList<String> lines = a.getAndStoreLines(inputFile);
            for (String line : lines) {
                a.processLine(line);
            }
            int[] pcInitValues = new int[a.pcStart.size()];
            for (int i = 0; i < pcInitValues.length; i++) {
                pcInitValues[i] = a.pcStart.get(i);
            }

            writeImageToFile(new ExecutableImage(a.createMemoryForImage(), pcInitValues), outputFile);
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void processLine(String line) {
        String[] split = line.split(" ");
        if (split[0].equals("") || line.startsWith("//")) {
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
                    currentSection = Sections.instructions;
                    labelToAddressMap.put(split[0], nextAddress);
                } else if (split[0].startsWith("$")) {
                    variableToAddressMap.put(split[0], nextAddress);
                    int value = Integer.decode(split[1]);
                    memoryImage.add(value);
                    nextAddress += Globals.WORD_SIZE * 8;
                } else {
                    throw new java.lang.RuntimeException("in data state found non matching instruction for line \n " + line);
                }
                break;
            case instructions:
                if (split[0].equals("#startpc")) {
                    currentSection = Sections.pcinit;
                } else if (split[0].startsWith("#")) {
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
                break;
            case pcinit:
                pcStart.add(labelToAddressMap.get(split[0]));
            default:
                break;
        }
    }

    private int parseInstruction(String instruction) {
        int toReturn = 1;
        String[] split = instruction.split(" ");
        //NOW WE ITERATE THROUGH THE SPLIT AND REPLACE LABELS WITH ADDRESSES
        for (int i = 0; i < split.length; i++) {
            if (split[i].length() >= 2) {
                Integer variable = variableToAddressMap.get(split[i].substring(1));
                Integer label = labelToAddressMap.get(split[i].substring(1));
                if (variable != null) {
                    split[i] = split[i].charAt(0) + "0x" + Integer.toHexString(variable);
                } else if (label != null) {
                    split[i] = split[i].charAt(0) + "0x" + Integer.toHexString(label);
                }
            }
        }
        // now we pass this along to the assembler
        AbstractInstruction assembler = opCodeToAssemblerMap.get(split[0]);
        String newInstruction = "";
        for (int i = 0; i < split.length; i++) {
            newInstruction += split[i] + " ";
        }
        if (assembler != null) {
            System.out.println(newInstruction);
            toReturn = assembler.assembleInstruction(newInstruction);
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

    public static void writeImageToFile(ExecutableImage image, String fileName) {
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(fileName));
            objStream.writeObject(image);
            objStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] createMemoryForImage() {
        byte[] toReturn = new byte[Globals.TOTAL_MEMORY_SIZE];
        int imageSize = memoryImage.size();
        int counter = 0;
        for (int i = 0; i < imageSize; i++) {
            byte[] fromInt = AbstractInstruction.intToByteArray(memoryImage.get(i));
            for (int j = 0; j < 4; j++) {
                toReturn[counter++] = fromInt[j];
            }
        }
        return toReturn;
    }
}
