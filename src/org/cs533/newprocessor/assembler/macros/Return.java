/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.macros;

/**
 *
 * @author amit
 */
public class Return extends AbstractMacro {

    static final String macroExtends = "ret";

    public String[] runMacro(String instruction) {
        String[] split = instruction.split(" ");
        String[] toReturn = new String[1];

        toReturn[0] = "jr r0";
        return toReturn;
    }

    public static String getInstructionThisMacroExtends() {
        return macroExtends;
    }
}
