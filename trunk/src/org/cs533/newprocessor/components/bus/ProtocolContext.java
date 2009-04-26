/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.LRUEvictHashTable;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author brandon
 */
public interface ProtocolContext<LineStates> {
    public MemoryInstruction getNextRequest();
    LRUEvictHashTable<CacheLine<LineStates>> getData();
}
