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
    public int state;
    public int address;//
    public int cachetag; // could be same as address for now
     public CacheLine(int address)
     {
       this.address = address;
       data = 0;
       this.state =2;
       this.shared = false;
     }

     public CacheLine()
     {

      data = 0;
      this.state =2;
      this.shared = false;
     }

    public CacheLine(int _data, boolean _shared)
    {
        data = _data;
        this.state = 2;//assume newly arriving data into cache is brought in from
                       //memory.  Thus, we are always at valid-exclusive
        this.shared = _shared;
    }
    public CacheLine( int _data, int _state)
    {
        this.data = _data;
        this.state = _state;
    }
}

