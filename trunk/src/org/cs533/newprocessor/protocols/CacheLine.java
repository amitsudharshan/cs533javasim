/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;

import org.cs533.newprocessor.protocols.Main.EventType;

/**
 *
 * @author Vivek
 */
public  class CacheLine
{
    //standard transitions in most all cache coherence protocols

    public enum Transition
    {
        pRead, pWrite, busRead, busWrite, pWriteMissServed, busInvalidate, ackInvalidate, busUpdate, updateAck
    }
  

    protected int address;
    protected int processor;

    //constructor
    public CacheLine(int _address, int _processor, boolean shared)
    {
        address = _address;
        processor = _processor;
    }

    public void processEvent(int EventType)
    {

          //not to be implemented
        // the subclass implements this
    }
   void processBusEvent(EventType eventType) {

         //not to be implemented
        // the subclass implements this
    }

    public void print()
    {
         System.out.println(Main.currentTime + " :  state of Cache Line " + address + " is "   );
    }



}
