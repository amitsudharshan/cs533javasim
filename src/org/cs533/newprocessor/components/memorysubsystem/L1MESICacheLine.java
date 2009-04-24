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
    public byte[] address;


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
     // this.data = data;
      //this.state = state;
      super(data, state);
      this.data = data;
      this.state = state;
    }


    // register memory as a client of bus

    // response = onMessage(currState, event);

    public int onMessage(int currentState, Event event, int status, boolean shared, int data )
    {
       if(event == Event.BusInvalidate)
       {
            action = busInvalidate(currentState, status, shared);
       }
       else if(event == Event.BusRead)
       {
           action = busRead(currentState, status, shared);
       }

       else if(event == Event.BusWrite)
       {
          action = busWriteMiss(currentState, status, shared);
       }
       else if(event == Event.PRead)
       {
           action = pRead(currentState, status, shared);
       }
       else if(event == Event.PWrite)
       {
           action = pWrite(currentState, status, shared, data);

       }
      return action;
    }



    public int busInvalidate(int currentState, int status, boolean shared)
    {
        if(currentState == 3)
        {
            state = 4;
            this.setValidBit(true);
        }
        return action;
    }

    public int busRead(int currentState, int status, boolean shared)
    {
        state = 3;
        this.setDirtyBit(false);
        return action;
    }
    public int pWrite(int currentState, int status, boolean shared, int data)
    {
        switch(currentState)
        {
            case 2-4:
                state = 1;
                setData(data);
                break;
            default:
               setData(data);
                break;
        }

        return action;
    }

    public int busWriteMiss(int currentState, int status, boolean shared)
    {
      switch(currentState)
        {
            case 1-3:
                state = 4;
                this.setValidBit(false);
                break;
            default:

              break;
        }
       return action;
     }


    public int busRead(int currentState, int status, int shared )
    {
        this.state = 3;
        return action;
    }


    public int pRead(int currentState, int status, boolean shared)
    {
        switch (currentState)
        {
            case 0:
                 if(shared)
                     state = 3;
                 else
                     state = 2;
                break;

            case 4:

              if(shared){currentState=3;}
              else
                {currentState = 2;}
              break;

            default:
                break;
     }
        return action;
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
    }

}
