/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.macros;

/**
 *
 * @author amit
 */
public class LoadValue extends AbstractMacro {

    static final String macroExtends = "loadv";


  

    public String[] runMacro(String instruction) {
        String[] split = instruction.split(" ");
        String[] toReturn = new String[3];
        String register = split[1];
        String variable = split[2];

        toReturn[0] = "lui " + register + " U" + variable;
        toReturn[1] = "ori " + register + " " + register + " L" + variable;
        toReturn[2] = "lw " + register + " " + register;
        return toReturn;
    }

    public String getInstructionThisMacroExtends() {
        return macroExtends;
    }
}
