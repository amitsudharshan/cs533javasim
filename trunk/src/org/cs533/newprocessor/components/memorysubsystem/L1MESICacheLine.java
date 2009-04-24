/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.memorysubsystem.CacheLine;

/**
 *
 * @author amit
 */
public class L1MESICacheLine extends CacheLine
{

      //public L1LineStates currentState;
    public int state;//1: Dirty 2: Exclusive 3: Shared 4: Invalid
    public int action;
    public int event;
    public int data;
    //public byte[] address;
    private boolean valid = false;
    private boolean dirty = false;

    public L1MESICacheLine(int _data, boolean shared)
    {
        super(_data, shared);
        data = _data;  
        this.valid = true;
        this.dirty = false;
    }


    public L1MESICacheLine(int _address, int _data)
    {

      super(_address, _data, 2);
      this.state = 2;

    }

      public L1MESICacheLine(int _address, int _data, int _processor)
    {
      super(_address, _data, 2, _processor);
      this.processor = _processor;
      this.valid = true;
      this.dirty = false;
    }



    public L1MESICacheLine() {
        this.data = 0;
        this.state = 2;
        this.valid = true;
        this.dirty = true;
    }


    public void setValidBit(boolean b) {
         this.valid = b;

    }

    private void setDirtyBit(boolean b) {
        this.dirty = b;
    }

     public enum Event
     {
        BusRead, BusWrite, PRead, PWrite, BusInvalidate
     }
     
     public enum Action
     {
         updateMM, requestUpdate
     }
  

     public enum L1LineStates {

       EXCL, Invalid, Dirty, Shared
     }

    
    // register memory as a client of bus

    // response = onMessage(currState, event);

    public int onMessage( Event event, int _address,  int _data )
    {
       if(event == Event.BusInvalidate)
       {
            state = busInvalidate(state, this.cache_entry.shared);
       }
       else if(event == Event.BusRead)
       {
           state = busRead(state, _address, cache_entry.shared);
       }

       else if(event == Event.BusWrite)
       {
          state = busWriteMiss(state, _address, cache_entry.shared);
       }
       else if(event == Event.PRead)
       {
           state = pRead(state, _address, _data);

       }
       else if(event == Event.PWrite)
       {
           state = pWrite(state, _address, cache_entry.shared, _data);

       }
      return state;
    }


    //Begin MESI  protocol state machine
    public int busInvalidate(int currentState, boolean shared)
    {
        if(currentState == 3)
        {
            state = 4;
            this.setValidBit(true);
        }
         System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
        return state;
    }

   //need to return the data for the external processor's cache line
    public int busRead(int currentState, int address, boolean shared)
    {
        state = 3;
        this.setDirtyBit(false);
        cache_entry.shared = true;  //global
         System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
        return state;
    }


    public int pWrite(int currentState, int address, boolean shared, int data)
    {
        switch(currentState)
        {
            case 2-4:
                state = 1;
                this.cache_entry.address = address;
                this.cachetag = address%4;//some arbitrary hash for now
                setData(data);
                this.setDirtyBit(true);
                break;
            default:
               state = 1;
               this.cache_entry.address = address;
               setData(data);
               this.setDirtyBit(true);
               break;
        }

        System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
        return state;
    }

    private boolean someOneElseHasIt()
    {
       return cache_entry.shared;
    }

    public int busWriteMiss(int currentState, int address, boolean shared)
    {
      switch(currentState)
        {
            case 1-3:
                state = 4;
                this.setValidBit(false);
                break;
            default:
                 this.setValidBit(false);
              break;
        }

       System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
       return state;
     }


    public int busRead(int currentState, int shared )
    {
        this.state = 3;
         System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
        return state;
    }


     //Example:  LD  $R1, M[$R2]

    public int pRead(int currentState, int address, int newlyReadData)
    {

        this.cache_entry.address = address;
        this.setData(newlyReadData);
        switch (currentState)
        {
            case 0:
                 if(someOneElseHasIt())
                 {
                     state = 3;
                 }
                 else
                 {
                     state = 2;
                 }
                 break;

            case 4:
              if(someOneElseHasIt())
                 {state=3;}
              else
                {state = 2;}
              break;

            default:
                break;
        }
         System.out.println("Processor #" + this.processor + ":   " +  currentState + " --> "  + state);
        return state;
   }


    public void setCurrentState(L1LineStates state)
    {
        this.state = state.ordinal();
    }

    public int getCurrentState()
    {
        return this.state;
    }

    public int getData()
    {
        return this.data;
    }

    public void setData(int data)
    {
        this.data = data;
        this.valid = true;
        this.dirty = false;
    }

}
