/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author Vivek
 */
public class CacheEntry {

    public boolean shared;
    public int address;
    public int data;
    public int[] processors;

    public CacheEntry(int _address, int _data, boolean _shared)
    {
        this.data = _data;
        this.address = _address;
        this.shared = _shared;

    }

    public int setCacheEntry(int processor,  int _address, int _data, boolean _shared)
    {


        this.data = _data;
        this.address = _address;
        this.shared = _shared;
        return _data;
    }

    public int whoHasThisEntry(int _address, int _data, boolean _shared)
    {

        return this.processors[0];
    }

}
