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

    private boolean valid = false;
    private boolean dirty = false;

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
    //public L1LineStates currentState;
    public int state;//1: Dirty 2: Exclusive 3: Shared 4: Invalid
    public int action;
    public int event;
    public int data;
    //public byte[] address;


     public enum L1LineStates {

       EXCL, Invalid, Dirty, Shared
     }

    public L1MESICacheLine(int i, boolean b)
    {
        super(i,b);
         data = i;
         b = false;

    }


    public L1MESICacheLine(int data, int state)
    {
    
      super(data, state);
      this.data = data;
      this.state = state;

    }


    // register memory as a client of bus

    // response = onMessage(currState, event);

    public int onMessage( Event event,  int data )
    {
       if(event == Event.BusInvalidate)
       {
            state = busInvalidate(state, shared);
       }
       else if(event == Event.BusRead)
       {
           state = busRead(state, shared);
       }

       else if(event == Event.BusWrite)
       {
          state = busWriteMiss(state, shared);
       }
       else if(event == Event.PRead)
       {
           state = pRead(state, shared, data);
       }
       else if(event == Event.PWrite)
       {
           state = pWrite(state,  shared, data);

       }
      return state;
    }



    public int busInvalidate(int currentState, boolean shared)
    {
        if(currentState == 3)
        {
            state = 4;
            this.setValidBit(true);
        }
        return state;
    }

    public int busRead(int currentState, boolean shared)
    {
        state = 3;
        this.setDirtyBit(false);
        shared = true;
        return state;
    }
    public int pWrite(int currentState, boolean shared, int data)
    {
        switch(currentState)
        {
            case 2-4:
                state = 1;
                setData(data);
                this.setDirtyBit(true);
                break;
            default:
               setData(data);
               this.setDirtyBit(true);
                break;
        }

        return state;
    }

    private boolean someOneElseHasIt( )
    {
       return shared;
    }
    public int busWriteMiss(int currentState, boolean shared)
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
       return state;
     }


    public int busRead(int currentState, int shared )
    {
        this.state = 3;
        return state;
    }


    public int pRead(int currentState, boolean shared, int newlyReadData)
    {
        switch (currentState)
        {
            case 0:
                 if(someOneElseHasIt())
                 {
                     state = 3;
                     this.setData(newlyReadData);
                 }
                 else
                     state = 2;
                break;

            case 4:

              if(shared)
                 {currentState=3;}
              else
                {currentState = 2;}
              break;

            default:
                break;
     }
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
