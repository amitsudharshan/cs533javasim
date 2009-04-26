/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

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
}

