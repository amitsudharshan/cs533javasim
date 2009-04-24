/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler;

import java.util.HashMap;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ALUInstructions.ALUImmediateInstructions.AddImmediateInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ALUInstructions.ALUImmediateInstructions.OrImmediateInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions.CompareAndSwapInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.HaltInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ALUInstructions.LoadUpperImmediateInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions.LoadWordInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.MemoryInstructions.StoreWordInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.ALUInstructions.ThreeRegisterALUInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.BranchInstructions.BranchIfEqualInstruction;
import org.cs533.newprocessor.assembler.instructionTypes.BranchInstructions.BranchIfLessThanOrEqualToZero;

/**
 *
 * @author amit
 */
public class OpcodeMetaData {

    public static HashMap<String, AbstractInstruction> populateOpCodeAssemblerMap() {
        HashMap<String, AbstractInstruction> opCodeToAssemblerMap = new HashMap<String, AbstractInstruction>();
        opCodeToAssemblerMap.put("add", new ThreeRegisterALUInstruction());
        opCodeToAssemblerMap.put("addi", new AddImmediateInstruction());
        opCodeToAssemblerMap.put("ori", new OrImmediateInstruction());
        opCodeToAssemblerMap.put("lui", new LoadUpperImmediateInstruction());
        opCodeToAssemblerMap.put("sw", new StoreWordInstruction());
        opCodeToAssemblerMap.put("lw", new LoadWordInstruction());
        opCodeToAssemblerMap.put("cas", new CompareAndSwapInstruction());
        opCodeToAssemblerMap.put("beq", new BranchIfEqualInstruction());
        opCodeToAssemblerMap.put("blez", new BranchIfLessThanOrEqualToZero());
        opCodeToAssemblerMap.put("halt", new HaltInstruction());
        return opCodeToAssemblerMap;
    }

    public static HashMap<Integer, AbstractInstruction> populateInstructionAssemblerMap() {
        HashMap<Integer, AbstractInstruction> instructionAssembler = new HashMap<Integer, AbstractInstruction>();
        HashMap<String, AbstractInstruction> opCodeMap = populateOpCodeAssemblerMap();
        for (AbstractInstruction instr : opCodeMap.values()) {
            if (instructionAssembler.containsKey(instr.getOpcode())) {
                throw new java.lang.RuntimeException("MULTIPLE OPCODES FOR " +
                        "ONE INSTRUCTION! for " + instr.toString());
            }
            instructionAssembler.put(instr.getOpcode(), instr);
        }
        return instructionAssembler;

    }
}
