/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;

/**
 *
 * @author Vivek
 */

import org.cs533.newprocessor.protocols.Main;
public class MESICacheLine extends CacheLine
{
    public enum State 
    {
        Modified, Exclusive, Shared, Invalid,  SharedToDirty
    }

 


    public State state;
    //need shared to determine initial state
    public MESICacheLine ( int _address, int _processor, boolean shared )
    {
        super(_address, _processor, shared);
        if(shared)
            state = State.Shared;
        else
            state = State.Exclusive;    
    }


    public static void processBusEvent(Event e)
    {
       int i;
        MESICacheLine myLine;

        switch(e.eventType)
        {
         case BusRead: //Bus Read
                      //loop through all the processors to see if one of them has e.address in its cache
                      //Accordingly, enqueue a "LineServed" Event with the appropriate source
                      //If it is in any cache, make those cache Lines process the event
                       int responder = -1; //-1 is memory,  if it is not found in any cache
                      for(i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine =
                                  (MESICacheLine) Main.caches[i].get(e.address)) != null)
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
                       for(i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine = (MESICacheLine) Main.caches[i].get(e.address)) != null)
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
                      myLine = new MESICacheLine(e.address, e.procNum, shared);

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
                      myLine = new MESICacheLine(e.address, e.procNum, shared);
                      myLine.processEvent(CacheLine.Transition.pWriteMissServed);
                      Main.caches[e.procNum].put(e.address, myLine);
                      e.eventType = Main.EventType.ExecuteNextInstruction; //execute next instruction from processor
                      e.timeStamp = e.timeStamp + 1;
                      Main.eventQueue.offer(e);
                      break;


                  case Invalidate:  //invalidate

                      for(i = 0; i < Main.numProcs; i++)
                      {
                          if((myLine =  (MESICacheLine) Main.caches[i].get(e.address)) != null)
                          {
                             if(i == e.source)
                                 myLine.processEvent(CacheLine.Transition.ackInvalidate);//INvalidation sent by i has been seen by everyone (ACK)
                              myLine.processEvent(CacheLine.Transition.busInvalidate);//someone sent a invalidate for cache line
                          }
                      }
                   break;

                  case Update:
                   /*
                  for(i = 0; i < numProcs; i++)
                      {
                          if((myLine =  (MESICacheLine) caches[i].get(e.address)) != null)
                          {
                             if(i == e.source)
                                 myLine.processEvent(7);//update sent by i has been seen by everyone (ACK)
                              myLine.processEvent(6);//someone sent a update for cache line
                          }
                      }
                    */
                   break;


                  default:    //bus Event with an address

                      System.out.println("Default  case: Event Type is  = " +  e.eventType);

                      System.out.println("procNum is " +  e.procNum );
                      myLine = (MESICacheLine)
                                 Main.caches[e.procNum].get(e.address);
                      //myLine.processBusEvent(e.eventType);
                      myLine.processBusEvent(e);
                    break;
              }

    }

public static void processorLoad(Event e, int address)
{

    MESICacheLine myLine;

     Main.totalReadAccesses++;
      if( ((myLine = (MESICacheLine) Main.caches[e.procNum].get(address)) != null)
                                     &&  (myLine.state != MESICacheLine.State.Invalid) )
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

      MESICacheLine myLine;
       Main.totalWriteAccesses++;
                             if(((myLine = (MESICacheLine) Main.caches[e.procNum].get(address)) != null)
                                 &&  (myLine.state != MESICacheLine.State.Invalid) )
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


    public void processEvent(CacheLine.Transition EventType)
    {

      State oldState = state;
       // System.out.println("proc " + processor + " address " + address + " Transition " + EventType);
        if (EventType == CacheLine.Transition.pRead )
            pRead();
        if (EventType == CacheLine.Transition.pWrite   )
            pWrite();
        if(EventType == CacheLine.Transition.busRead)
            busRead();
        if(EventType == CacheLine.Transition.busWrite)
            busWrite();
        if(EventType == CacheLine.Transition.pWriteMissServed)
            pWriteMissServed();
        if(EventType == CacheLine.Transition.busInvalidate)
            busInvalidate();
        if(EventType == CacheLine.Transition.ackInvalidate)
            ackInvalidate();

        
        if(state != oldState)
        {
           System.out.print(Main.currentTime );
           Main.printBlanks(5*processor);
           System.out.println("p #" + processor + " cache line " + address + " transition " +  oldState +   " -> "  + state);
        }
           //this.print();


    }




    public int pRead()
    {
         //state remains unchanged

         return 1;
    }
    public int pWrite()
    {
       
        if ((state == State.Exclusive)  ||  (state == State.Modified))
        {
            state = State.Modified;
            Event e = new Event(Main.currentTime + 1 ,processor , processor , Main.EventType.ExecuteNextInstruction );
            Main.eventQueue.offer(e);
        }
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


   public  void print()
    {
      System.out.println(Main.currentTime + " :  state of MESI Cache Line " + address + " is " + state.name()  );
    }


}
