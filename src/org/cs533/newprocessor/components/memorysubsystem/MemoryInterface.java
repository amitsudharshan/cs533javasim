/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author amit
 */
public interface MemoryInterface {
    // This method may only be invoked once per runPrep cycle,
    // to ensure requests arrive in a deterministic order.
    public void enqueueMemoryInstruction(MemoryInstruction instr);
}
