/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.macros;

import java.util.HashMap;
import org.cs533.newprocessor.assembler.macros.LoadValue;

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

    public abstract String getInstructionThisMacroExtends();

    public static void addToMap(AbstractMacro macro) {
        instructionToMacro.put(macro.getInstructionThisMacroExtends(), macro);
    }

    public static void createMacroMap() {
        Load load = new Load();
        LoadValue loadV = new LoadValue();
        Return ret = new Return();
        instructionToMacro.put(load.getInstructionThisMacroExtends(), load);
        instructionToMacro.put(loadV.getInstructionThisMacroExtends(), loadV);
        instructionToMacro.put(ret.getInstructionThisMacroExtends(), ret);
    }
}
