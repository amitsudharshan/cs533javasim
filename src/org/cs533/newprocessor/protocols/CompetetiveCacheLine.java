/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;

/**
 *
 * @author Vivek
 */

//  use a barrier instruction and enqueue it  on the eventQueue
// since  we are simulating, use a simple barrier.  

public class CompetetiveCacheLine extends CacheLine
{

    public enum State
    {
        Modified, Exclusive, Shared, Invalid,  SharedToDirty,  Blocked, Dirty_with_sharers
    }

    public State state;


    //need shared to determine initial state
    public CompetetiveCacheLine ( int _address, int _processor, boolean shared )
    {
        super(_address, _processor, shared);
        if(shared)
            state = State.Shared;
        else
            state = State.Exclusive;
    }


    public void processEvent(int EventType)
    {
        if (EventType == 1)
            pRead();
        if (EventType == 2)
            pWrite();
        if(EventType == 3)
            busRead();
        if(EventType == 4)
            busWrite();
        if(EventType == 5)
            pWriteMissServed();
        if(EventType == 6)
             busInvalidate();
        if(EventType == 7)
             ackInvalidate();

        if(EventType == 8)
        {
            pFirstWrite();
        }
        if(EventType ==9)
        {
            pLastWrite();
        }

       this.print();

    }


    public int pRead()
    {
         //state remains unchanged

         return 1;
    }
    public int pWrite()
    {

        if(state == State.Exclusive)
            state = State.Modified;
        else if(state == State.Shared)
        {
            Event e = new Event();
            e.timeStamp = java.lang.Math.max(Main.currentTime, Main.nextBusAvailable  )   + Main.busRequestTime;
            e.eventType = Main.EventType.Invalidate;//invalidate
            e.procNum = -1;//bus broadcast
            e.source = processor;
            e.address = this.address;
            Main.eventQueue.offer(e);
            state = State.SharedToDirty;
            Main.pendingBusRequests[processor] = e;
        }
        //if it is already dirty nothing needs to be done
        return 1;
    }
    public int busRead()
    {
        state = State.Shared;
         return 1;
    }
    public int busWrite()
    {
      state = State.Invalid;
      return 1;
    }

    public int busInvalidate()
    {
      if(state == State.SharedToDirty)
      {
          state = State.Modified;

          Event e = Main.pendingBusRequests[processor];
          Main.eventQueue.remove(e);
          e.eventType = Main.EventType.BusWriteMiss;
          e.timeStamp =   java.lang.Math.max( e.timeStamp, Main.nextBusAvailable) + Main.busRequestTime;
          Main.eventQueue.offer(e);
      }
      else state = State.Invalid;

        return 1;
    }


    public int busBlock()
    {

        return -1; //not implemented
    }


    private int pWriteMissServed()
    {
       state = State.Modified;
       return 1;
    }

        private void ackInvalidate()
        {
           if(state == State.SharedToDirty)
           {
             state = State.Modified;
             Event e = new Event(Main.currentTime, processor, processor, Main.EventType.ExecuteNextInstruction);//execute next instruction
              Main.eventQueue.offer(e);
           }

           else
               System.out.println("error: acknowledgement for invalidation received in wrong state.");
        }

     private void pFirstWrite()
     {


     }



    private void pLastWrite()
    {

    }

  }
