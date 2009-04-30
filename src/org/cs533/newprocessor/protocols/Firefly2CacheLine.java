

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;

/**
 *
 * @author Vivek
 */
public class Firefly2CacheLine extends CacheLine
{
    public enum State
    {
        Modified, Exclusive, Shared,
         Invalid,  SharedToDirty
    }

    public State state;
    //need shared to determine initial state
    public Firefly2CacheLine ( int _address, int _processor, boolean shared )
    {
        super(_address, _processor, shared);
        if(shared)
            state = State.Shared;
        else
            state = State.Exclusive;
    }

    public void processEvent(CacheLine.Transition EventType)
    {

      switch( EventType)
      {
          case pRead:     pRead(); break;
          case pWrite: pWrite(); break;
          case busRead:  busRead(); break;
          case busWrite:   busWrite(); break;
          case pWriteMissServed: pWriteMissServed();break;
          case busInvalidate: busInvalidate();break;
          case ackInvalidate: ackInvalidate();break;
          case busUpdate: busUpdate(); break;
          case updateAck: updateAck(); break;
          default:   break;
      }

       this.print();

    }

   void processBusEvent(Main.EventType eventType)
   {
       //MESI  doesn't have any additional non-standard events
       System.err.println("Error:  unsupported bus event!  "   +  eventType.name().toString());
   }


   public void busUpdate()
       {

       }

    private void updateAck() {

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
            e.timeStamp = java.lang.Math.max(Main.currentTime, Main.nextBusAvailable  )   + Main.busUpdateTime;
            e.eventType = Main.EventType.Update;//invalidate
            e.procNum = -1;//bus broadcast
            e.source = processor;
            e.address = this.address;
            Main.eventQueue.offer(e);
            Main.pendingBusRequests[processor] = e;
        }
        //if it is already dirty nothing needs to be done
        return -1; //not implemented
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
        System.err.println("  Error:  There shouldn't be invalidation in Firefly protocol.");
        return -1;
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

   public  void print()
    {
      System.out.println(Main.currentTime + " :  state of MESI Cache Line " + address + " is " + state.name()  );
    }
}
