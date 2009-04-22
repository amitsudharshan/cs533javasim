/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author amit
 */
public class CacheLine {
    public int data;
    public boolean shared;


    public CacheLine(int _data, boolean _shared)
    {
        data = _data;
        this.shared = _shared;
    }

}
