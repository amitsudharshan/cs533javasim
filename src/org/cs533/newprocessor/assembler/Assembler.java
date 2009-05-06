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
import org.cs533.newprocessor.assembler.macros.AbstractMacro;
import org.cs533.newprocessor.simulator.ExecutableImage;

/**
 *
 * @author amit
 */
public class Assembler {

    public enum Sections {

        start, data, instructions, pcinit
    }

    enum ParserReturnValues {

        success, failure, missinglabel, missingvariable
    }
    Sections currentSection = Sections.start;
    int nextAddress = 0;
    HashMap<String, Integer> variableToAddressMap = new HashMap<String, Integer>();
    HashMap<String, Integer> labelToAddressMap = new HashMap<String, Integer>();
    HashMap<String, AbstractInstruction> opCodeToAssemblerMap;
    ArrayList<Integer> memoryImage = new ArrayList<Integer>();
    ArrayList<Integer> pcStart = new ArrayList<Integer>();
    ArrayList<int[]> labelsToFix = new ArrayList<int[]>();
    ArrayList<int[]> instructionsToFix = new ArrayList<int[]>();

    public Assembler() {
        opCodeToAssemblerMap = OpcodeMetaData.populateOpCodeAssemblerMap();
    }

    class ParsedInstruction {

        int instruction;
        ParserReturnValues flag;

        public ParsedInstruction(int instruction, ParserReturnValues flag) {
            this.instruction = instruction;
            this.flag = flag;
        }

        public int getInstruction() {
            return instruction;
        }

        public ParserReturnValues getFlag() {
            return flag;
        }
    }

    public static void main(String[] args) throws Exception {
        String inputFile = "/home/amit/NetBeansProjects/newcs533javasim/src/org/cs533/asm/matrixmultiply.c";
        String outputFile = "/home/amit/asm/a.out";
        if (args.length >= 2) {
            inputFile = args[0];
            outputFile = args[1];
        }
        assembleFile(inputFile, outputFile);
    }

    public static void assembleFile(String inputFile, String outputFile) {
        writeImageToFile(getFullImage(inputFile), outputFile);
    }

    public static ExecutableImage getFullImage(String inputFile) {
        try {
            Assembler a = new Assembler();
            ArrayList<String> lines = a.getAndStoreLines(inputFile);
            int counter = 0;
            //PASS ONE WILL GET THE LABELS IN AND MOST INSTRUCTIONS
            for (String line : lines) {
                a.processLine(line, counter++);
            }
            a.variableToAddressMap.put("$heap", a.nextAddress);
            int[] pcInitValues = new int[a.pcStart.size()];
            for (int i = 0; i < pcInitValues.length; i++) {
                pcInitValues[i] = a.pcStart.get(i);
                System.out.println("PC INIT VALUE IS " + pcInitValues[i]);
            }
            System.out.flush();
            System.err.flush();
            for (int[] toFix : a.instructionsToFix) {
                String line = lines.get(toFix[1]);
                ParsedInstruction instr = a.parseInstruction(line);
                if (instr.getFlag() == ParserReturnValues.failure ||
                        instr.getFlag() == ParserReturnValues.missinglabel ||
                        instr.getFlag() == ParserReturnValues.missingvariable) {
                    throw new java.lang.RuntimeException("failure on pass two for instruction: \n\t" + line);
                } else {
                    a.memoryImage.set(toFix[0], instr.getInstruction());
                }

            }
            return new ExecutableImage(a.createMemoryForImage(), pcInitValues);
        } catch (IOException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void processLine(String line, int lineIndex) {
        String[] split = line.split(" ");
        if (split.length < 1 || split[0].equals("") || line.startsWith("//")) {
            // these lines should be ignored they are blank or comments
            return;
        }
        switch (currentSection) {
            case start:
                if (split[0].equals(".data")) {
                    currentSection = Sections.data;
                } else {
                    throw new java.lang.RuntimeException("ERROR FILE MUST START WITH data section");
                }
                break;
            case data:
                if (split[0].equals(".instructions")) {
                    currentSection = Sections.instructions;
                } else if (split[0].startsWith("$")) {
                    int toAdd = 0;
                    int nextOffset = 0;
                    int brakIndex = split[0].indexOf("[");
                    if (brakIndex >= 0) {
                        int rIndex = split[0].indexOf("]");
                        nextOffset = Integer.parseInt(split[0].substring(brakIndex + 1, rIndex));
                        System.out.println("got a nextOffset with value " + nextOffset + " for variable: " + split[0]);
                        split[0] = split[0].substring(0,brakIndex);
                    } else {
                        long value = Long.decode(split[1]);
                        toAdd = (int) (value & 0xFFFFFFFF);
                    }
                    variableToAddressMap.put(split[0], nextAddress);
                    memoryImage.add(toAdd);
                    nextAddress = (nextAddress + nextOffset) + (Globals.WORD_SIZE * 1);

                } else {
                    throw new java.lang.RuntimeException("in data state found non matching instruction for line \n " + line);
                }
                break;
            case instructions:
                if (split[0].equals(".startpc")) {
                    currentSection = Sections.pcinit;
                } else if (split[0].startsWith("#")) {
                    System.out.println("labels are : " + split[0]);
                    labelToAddressMap.put(split[0], nextAddress);
                } else {
                    ParsedInstruction instruction = parseInstruction(line);
                    if (instruction.getFlag() == ParserReturnValues.failure) {
                        throw new java.lang.RuntimeException("found an invalid instruction with line: \n\t" + line);
                    } else if (instruction.getFlag() == ParserReturnValues.success) {
                        memoryImage.add(instruction.getInstruction());
                        nextAddress = nextAddress + (Globals.WORD_SIZE * 1);
                    } else if (instruction.getFlag() == ParserReturnValues.missinglabel || instruction.getFlag() == ParserReturnValues.missingvariable) {
                        memoryImage.add(-1);
                        instructionsToFix.add(new int[]{memoryImage.size() - 1, lineIndex});
                        nextAddress = nextAddress + (Globals.WORD_SIZE * 1);
                    }
                }
                break;
            case pcinit:
                pcStart.add(labelToAddressMap.get(split[0]));
            default:
                break;
        }
    }

    /**
     * This method parses the string instruction into an int for the memory image
     * @param instruction the line containing the instruction
     */
    private ParsedInstruction parseInstruction(String instruction) {
        ParsedInstruction toReturn = new ParsedInstruction(-1, ParserReturnValues.failure);
        String[] split = instruction.split(" ");
        //NOW WE ITERATE THROUGH THE SPLIT AND REPLACE LABELS WITH ADDRESSES
        for (int i = 0; i < split.length; i++) {
            if (split[i].length() >= 2) {
                Integer variable = variableToAddressMap.get(split[i].substring(1));
                Integer label = labelToAddressMap.get(split[i]);

                if (split[i].indexOf("#") >= 0 && label == null) {
                    return new ParsedInstruction(-1, ParserReturnValues.missinglabel);
                } else if (split[i].trim().indexOf("$heap") >= 0 && variable == null) {
                    return new ParsedInstruction(-1, ParserReturnValues.missingvariable);
                } else if (split[i].trim().indexOf("$heap") >= 0) {
                    System.out.println("GOT A HEAP VALUE OF " + variable);
                }
                if (variable != null) {
                    split[i] = split[i].charAt(0) + "0x" + Integer.toHexString(variable);
                } else if (label != null) {
                    split[i] = "0x" + Integer.toHexString(label);
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
            int instr = assembler.assembleInstruction(newInstruction);
            return new ParsedInstruction(instr, ParserReturnValues.success);
        }
        return toReturn;
    }

    public ArrayList<String> getAndStoreLines(String file) throws IOException {
        BufferedReader bRead = new BufferedReader(new FileReader(file));
        ArrayList<String> tokens = new ArrayList<String>();
        String line = null;
        while ((line = bRead.readLine()) != null) {
            if (line.compareTo("") != 0) {
                String[] newInstructions = AbstractMacro.getMacroForInstruction(line);
                if (newInstructions != null) {
                    for (String instr : newInstructions) {
                        tokens.add(instr.trim());
                    }
                } else {
                    tokens.add(line.trim());
                }
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
