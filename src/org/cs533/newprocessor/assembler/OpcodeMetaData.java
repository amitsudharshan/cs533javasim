/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import java.util.HashMap;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ALUImmediateInstructions.OrImmediateInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.HaltInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.LoadUpperImmediateInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.LoadWordInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.StoreWordInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ThreeRegisterALUInstruction;

/**
 *
 * @author amit
 */
public class OpcodeMetaData {

    public static HashMap<String, AbstractInstruction> populateOpCodeAssemblerMap() {
        HashMap<String, AbstractInstruction> opCodeToAssemblerMap = new HashMap<String, AbstractInstruction>();
        opCodeToAssemblerMap.put("add", new ThreeRegisterALUInstruction());
        opCodeToAssemblerMap.put("ori", new OrImmediateInstruction());
        opCodeToAssemblerMap.put("lui", new LoadUpperImmediateInstruction());
        opCodeToAssemblerMap.put("sw", new StoreWordInstruction());
        opCodeToAssemblerMap.put("lw", new LoadWordInstruction());
        opCodeToAssemblerMap.put("halt", new HaltInstruction());
        return opCodeToAssemblerMap;
    }

    public static HashMap<Integer, AbstractInstruction> populateInstructionAssemblerMap() {
        HashMap<Integer, AbstractInstruction> instructionAssembler = new HashMap<Integer, AbstractInstruction>();
        HashMap<String, AbstractInstruction> opCodeMap = populateOpCodeAssemblerMap();
        for (AbstractInstruction instr : opCodeMap.values()) {
            instructionAssembler.put(instr.getOpcode(), instr);
        }
        return instructionAssembler;

    }
}
