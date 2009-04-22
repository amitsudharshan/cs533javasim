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
public class L1MESI extends CacheLine
{



    public enum L1LineStates {

       EXCL, Invalid, Dirty, Shared
    }

    public enum Event{
        BusRead, BusWrite, PRead, PWrite, BusInvalidate
    }
    public L1LineStates currentState;
    public int state;//1: Dirty 2: Exclusive 3: Shared 4: Invalid
    public int action;
    public int event;
    public int data;
    public byte[] address;

    public L1MESI(int i, boolean b)
    {
         data = i;
         b = false;
    }

 


    

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
            state = 4;
        return action;
    }

    public int busRead(int currentState, int status, boolean shared)
    {
        state = 3;
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
                break;
            default:

              break;
        }
       return action;
     }


    public int busRead(int currentState, int status, int shared )
    {
        currentState = 3;
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


    public L1MESI(int data, L1LineStates state)
    {
        this.data = data;
        currentState = state;
    }

    public void setCurrentState(L1LineStates state)
    {
        currentState = state;
    }


    public L1LineStates getCurrentState() {
        return currentState;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
