/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.macros;

/**
 *
 * @author amit
 */
public class Load extends AbstractMacro {

    static final String macroExtends = "load";

    public String[] runMacro(String instruction) {
        String[] split = instruction.split(" ");
        String[] toReturn = new String[2];
        String register = split[1];
        String variable = split[2];

        toReturn[0] = "lui " + split[1] + " U" + split[2];
        toReturn[1] = "ori " + split[1] + " " + split[1] +" L" + split[2];
        return toReturn;
    }

    public static String getInstructionThisMacroExtends() {
        return macroExtends;
    }
}
