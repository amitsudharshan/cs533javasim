/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.macros;

import java.util.HashMap;

/**
 *
 * @author amit
 */
public abstract class AbstractMacro {

    public static HashMap<String, AbstractMacro> instructionToMacro = new HashMap<String, AbstractMacro>();


    static {
        createMacroMap();
    }

    public static String[] getMacroForInstruction(String line) {
        if (instructionToMacro.size() == 0) {
            createMacroMap();
        }
        line = line.trim();
        AbstractMacro macro = null;
        String[] newInstructions = null;
        String[] split = line.split(" ");
        macro = instructionToMacro.get(split[0]);
        if (macro != null) {
            newInstructions = macro.runMacro(line);
        }
        return newInstructions;
    }

    public abstract String[] runMacro(String instruction);

    public static void createMacroMap() {
        instructionToMacro.put(Load.getInstructionThisMacroExtends(), new Load());
        instructionToMacro.put(LoadValue.getInstructionThisMacroExtends(), new LoadValue());
        instructionToMacro.put(Return.getInstructionThisMacroExtends(), new Return());
    }
}
