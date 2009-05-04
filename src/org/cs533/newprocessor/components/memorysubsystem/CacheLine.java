/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

/**
 *
 * @author amit
 */
public class CacheLine<LineStates> {
    public LineStates state;
    public int address;
    public byte[] data;

     public CacheLine(int _address, byte[] _data, LineStates _state)
    {
        address = _address;
        data = _data;
        state = _state;
    }
     public CacheLine(MemoryInstruction response, LineStates _state)
     {
        assert response.type == InstructionType.Load;
        address = response.getInAddress();
        data = response.getOutData();
        state = _state;
     }
}

