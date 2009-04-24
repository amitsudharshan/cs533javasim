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
  
    public int state;
    public CacheEntry cache_entry;
    public int cachetag; // could be same as address for now
    public int processor;  //indicates which processor's cache this cacheLine is part of
     public CacheLine(int address)
     {
       cache_entry = new CacheEntry(address, 0, false);
       this.state =2;
     }

     public CacheLine()
     {
       cache_entry = new CacheEntry(0, 0, false);
      this.state =2;
      this.processor = -1;//belongs to no processor
     }

    public CacheLine(int _data, boolean _shared)
    {
        cache_entry = new CacheEntry(0, _data, _shared);
        this.state = 2;//assume newly arriving data into cache is brought in from
                       //memory.  Thus, we are always at valid-exclusive
        this.processor = -1;
    }
    public CacheLine( int _data, int _state)
    {
        cache_entry = new CacheEntry (0, _data, false);
        this.state = _state;
        this.processor = -1;
    }
     public CacheLine( int _data, int _state, int _myProcessor)
    {
           cache_entry = new CacheEntry(0, _data, false);
        this.state = _state;
        this.processor = _myProcessor;
    }

     public CacheLine(int _address, int _data, int _state, int _myProcessor)
    {
        cache_entry = new CacheEntry(_address, _data, false);
        this.state = _state;
        this.processor = _myProcessor;
    }

}

