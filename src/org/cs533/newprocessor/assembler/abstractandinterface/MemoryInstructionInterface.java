/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.assembler.abstractandinterface;

import org.cs533.newprocessor.components.core.RegisterFile;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public interface MemoryInstructionInterface {

    public MemoryInstruction getMemoryInstruction(RegisterFile rFile);

    public void handleWriteBack(byte[] toWriteBack, RegisterFile rFile);
}
