/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;

/**
 *
 * @author Vivek
 *
 *
 */



public class FireflyCacheLine extends CacheLine
{
    public enum State
    {
        Modified, Exclusive, Shared, Invalid,  SharedToDirty,   UpdateInProgress
    }

    public State state;
    //need shared to determine initial state
    public FireflyCacheLine ( int _address, int _processor, boolean shared )
    {
        
        super(_address, _processor, shared);

  // System.out.println("Firefly Line created");
        if(shared)
            state = State.Shared;
        else
            state = State.Exclusive;
    }

    public void processEvent(CacheLine.Transition EventType)
    {


      State oldState = state;
       // System.out.println("proc " + processor + " address " + address + " Transition " + EventType);
      switch( EventType)
      {
          case pRead:     pRead();    break;
          case pWrite:    pWrite();   break;
          case busRead:   busRead();  break;
          case busWrite:  busWrite(); break;
          case pWriteMissServed: pWriteMissServed(); break;
          //case busInvalidate: busInvalidate();break;
          //case ackInvalidate: ackInvalidate();break;
          case busUpdate: busUpdate();               break;
          case updateAck: updateAck();               break;
          default:   break;
      }
        if(state != oldState)
        {
          System.out.print( Main.currentTime);
         Main.printBlanks(5*processor);
           System.out.println( " 0x" + address + " " +  oldState +   " -> "  + state);
    }   }

   void processBusEvent(Main.EventType eventType) 
   {
       //Firefly doesn't have any additional non-standard events
       System.err.println("Error:  unsupported bus event!  "   +  eventType.name().toString());
    }



    public static void processBusEvent(Event e)
    {
  
        FireflyCacheLine myLine;

        switch(e.eventType)
        {
         case BusRead: //Bus Read
                      //loop through all the processors to see if one of them has e.address in its cache
                      //Accordingly, enqueue a "LineServed" Event with the appropriate source
                      //If it is in any cache, make those cache Lines process the event
                       int responder = -1; //-1 is memory,  if it is not found in any cache
                      for(int i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine =
                                  (FireflyCacheLine) Main.caches[i].get(e.address)) != null)
                          {
                              responder = i;
                              myLine.processEvent(CacheLine.Transition.busRead);//someone sent a read request for cache line
                          }
                      }
                      e.eventType = Main.EventType.LineServedRead; //lineServed event
                      e.procNum = e.source;
                      e.source = responder;
                      e.timeStamp = java.lang.Math.max(Main.nextBusAvailable, e.timeStamp) + Main.sendDataTime;
                      Main.eventQueue.offer(e);
                      break;
                  case BusWriteMiss: //Bus Write Miss
                       responder = -1;
                        //same as above:  enqueue a "LineServed" and process the "BusWrite"
                       for(int i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine = (FireflyCacheLine) Main.caches[i].get(e.address)) != null)
                          {
                              responder = i;
                              myLine.processEvent(CacheLine.Transition.busWrite);//Protocol someone sent a bus write for cache line
                          }
                      }

                      e.eventType = Main.EventType.LineServedWrite; //lineServed e ent
                      e.procNum = e.source;
                      e.source = responder;
                      e.timeStamp = java.lang.Math.max(Main.nextBusAvailable, e.timeStamp) + Main.sendDataTime;
                      Main.eventQueue.offer(e);
                      break;
                  case LineServedRead: //Line Served
                       //insert a cache line into the processor's cache
                      //does not consider evictions for now
                      boolean shared;
                      if(e.source == -1) //memory served it
                           shared = false;
                      else
                          shared = true;

                      assert(e.procNum  < 0 );
                      myLine = new FireflyCacheLine(e.address, e.procNum, shared);

                      Main.caches[e.procNum].put(e.address, myLine);
                      e.eventType = Main.EventType.ExecuteNextInstruction; //execute next instruction from processor
                      e.timeStamp = e.timeStamp + 1;
                      Main.eventQueue.offer(e);
                      break;

                     case LineServedWrite: //Line Served
                       //insert a cache line into the processor's cache
                      //does not consider evictions for now
                      if(e.source == -1) //memory served it
                           shared = false;
                      else
                          shared = true;

                      assert(e.procNum  < 0 );
                      myLine = new FireflyCacheLine(e.address, e.procNum, shared);
                      myLine.processEvent(CacheLine.Transition.pWriteMissServed);
                      Main.caches[e.procNum].put(e.address, myLine);
                      e.eventType = Main.EventType.ExecuteNextInstruction; //execute next instruction from processor
                      e.timeStamp = e.timeStamp + 1;
                      Main.eventQueue.offer(e);
                      break;


                  case Invalidate:  //invalidate
                     /*
                      for(int i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine =  (FireflyCacheLine) Main.caches[i].get(e.address)) != null)
                          {
                             if(i == e.source)
                                 myLine.processEvent(CacheLine.Transition.ackInvalidate);//INvalidation sent by i has been seen by everyone (ACK)
                              myLine.processEvent(CacheLine.Transition.busInvalidate);//someone sent a invalidate for cache line
                          }
                      }
                      */
                      System.err.println("No invalidates in this protocol.");
                   break;

                  case Update:
                 
                  for(int i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine =  (FireflyCacheLine) Main.caches[i].get(e.address)) != null)
                          {
                             if(i == e.source)
                                 myLine.processEvent(CacheLine.Transition.updateAck);//update sent by i has been seen by everyone (ACK)
                              myLine.processEvent(CacheLine.Transition.busUpdate);//someone interrupted your update---do I care?
                          }
                      }
           
                   break;


                  default:    //bus Event with an address

                      System.out.println("Default  case: Event Type is  = " +  e.eventType);

                      System.out.println("procNum is " +  e.procNum );
                      myLine = (FireflyCacheLine)
                                 Main.caches[e.procNum].get(e.address);
                      myLine.processBusEvent(
                              e.eventType);
                    break;
              }

    }

public static void processorLoad(Event e, int address)
{

    FireflyCacheLine myLine;

     Main.totalReadAccesses++;
      if( ((myLine = (FireflyCacheLine) Main.caches[e.procNum].get(address)) != null)
                                     &&  (myLine.state != FireflyCacheLine.State.Invalid) )
                                { //hit
                                      myLine.processEvent(CacheLine.Transition.pRead);
                                      e.timeStamp += 1;
                                      Main.eventQueue.offer(e);
                                }
                                else
                                {
                                    Main.totalReadMisses++;
                                    e.eventType = Main.EventType.BusRead;//bus Read
                                    e.source = e.procNum;
                                    e.procNum = -1;//this event is broadcast to everyone
                                    e.address = address;
                                    e.timeStamp = java.lang.Math.max( e.timeStamp, Main.nextBusAvailable) + Main.busRequestTime;
                                    Main.nextBusAvailable = e.timeStamp;
                                    Main.eventQueue.offer(e);
                                }
}

public  static void processorStore(Event e, int address)
{

      FireflyCacheLine myLine;
       Main.totalWriteAccesses++;
                             if(((myLine = (FireflyCacheLine) Main.caches[e.procNum].get(address)) != null)
                                 &&  (myLine.state != FireflyCacheLine.State.Invalid) )
                                {
                                    myLine.processEvent(CacheLine.Transition.pWrite);
                                }

                                else
                                {
                                    Main.totalWriteMisses++;
                                    e.eventType =  Main.EventType.BusWriteMiss;//bus write
                                    e.source = e.procNum;
                                    e.procNum = -1;//this event is broadcast to everyone
                                    e.address = address;
                                    e.timeStamp = java.lang.Math.max( e.timeStamp, Main.nextBusAvailable) + Main.busRequestTime;
                                    Main.nextBusAvailable = e.timeStamp;
                                    Main.eventQueue.offer(e);
                                }
    }








     public void busUpdate()
     {
         //cache line just copies the updated data sent from another processor,
         //since we're not keeping track of data, nothing needs to be done here
         /*
      if(state == State.UpdateInProgress)
       {
          //someone else updated you while your update has not yet appeared on the bus
          state = State.Shared;
          Event e = Main.pendingBusRequests[processor];
          Main.eventQueue.remove(e);
          //change values of event fields to reflect acknowledgement
          e.eventType = Main.EventType.ExecuteNextInstruction;//special case< -- check this
          e.timeStamp =   java.lang.Math.max( e.timeStamp, Main.nextBusAvailable) + Main.busUpdateTime;
          Main.eventQueue.offer(e);
      }
          
       else
          */
           state = State.Shared;
     }


    private void updateAck() 
    {
        //now  this processor knows all other processors have updated
       if(state == State.UpdateInProgress)
         {
          state = State.Shared;
          Event e = new Event(Main.currentTime, processor, processor, Main.EventType.ExecuteNextInstruction);//execute next instruction
          Main.eventQueue.offer(e);
         }
      else
         System.out.println("error: acknowledgement for update received in wrong state.");
    }
   
   public int pRead()
    {
         //state remains unchanged
         return 1;
    }
    public int pWrite()
    {
        if(state == State.Exclusive  || state == State.Modified)
        {
            state = State.Modified;
            Event e = new Event(Main.currentTime + 1, processor, processor, Main.EventType.ExecuteNextInstruction);
            Main.eventQueue.offer(e);
        }
        else if(state == State.Shared)
        {
            //Event e = new Event(0 , processor, -1, Main.EventType.Invalidate);
            Event e = new Event();
            e.timeStamp =  (java.lang.Math.max(Main.currentTime, Main.nextBusAvailable  )   + Main.busUpdateTime );
            e.eventType = Main.EventType.Update;//update
            e.procNum = -1;//bus broadcast
            e.source = processor;
            e.address = this.address;
            Main.eventQueue.offer(e);
            state = State.UpdateInProgress;
            Main.pendingBusRequests[processor] = e;
        }
        else
            System.err.println("error: processor executed ahead while cache controller is waiting for update to finish.");

        return 1; 
    }

    public int busRead()
    {
        state = State.Shared;
        //TODO: also update Main Memory
         return 1;
    }


    //TODO: is this same as busUpdate???
    public int busWrite()
    {
      state = State.Shared;
      return 1;
    }


    //not used
    public int busInvalidate()
    {
        /*
      if(state == State.SharedToDirty)
      {
          //someone else invalidated you while your invalidation has not yet appeared on the bus
          state = State.Invalid;
          Event e = Main.pendingBusRequests[processor];
          Main.eventQueue.remove(e);
          e.eventType = Main.EventType.BusWriteMiss;
          e.timeStamp =   java.lang.Math.max( e.timeStamp, Main.nextBusAvailable) + Main.busRequestTime;
          Main.eventQueue.offer(e);
      }

      else state = State.Invalid;

       */

        System.err.println("Error:  there is no invalidation event in this protocol.");
        return 1;
    }

    public int busBlock()
    {
        return -1; //not implemented
    }


    private int pWriteMissServed()
    {

      if(state != State.Shared)
        state = State.Modified;

      return 1;
    }

    private void ackInvalidate()
    {
      if(state == State.SharedToDirty)
         {
          System.out.print("*******proc " + processor + " cache line " + address + " transition " +  state.name() +   " -> " );
          state = State.Modified;
          System.out.println(state.name());
          Event e = new Event(Main.currentTime, processor, processor, Main.EventType.ExecuteNextInstruction);//execute next instruction
          Main.eventQueue.offer(e);
         }
      else
          System.out.println("error: acknowledgement for invalidation received in wrong state.");
    }

   public  void print()
    {
      System.out.println(Main.currentTime + " :  state of Firefly Cache Line " + address + " is " + state.name()  );
   }
}