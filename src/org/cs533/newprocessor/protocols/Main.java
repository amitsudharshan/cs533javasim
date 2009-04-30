/*
 *  Design Decisions:
 *   1. Processor is blocked at LD/ST  until it is completed
 *  2.  Event-driven loop is the driver
 *   3. No queue for transactions waiting for the bus because
 *      when the event that schedules a bus transaction executes,
 *      we know exactly when the bus will be available for it.
 *      For this purpose, we use a global variable 'nextBusAvailableAt'
 *      This works because the executing event is the earliest event in the queue.
 *   4. Content of memory and registers is not modeled, nor do address traces include that info
 *      The only types of instruction traces allowed are 'LD x',  'ST x', and 'OTHER'.  'OTHER' could be
 *      branch or ALU instructions.  The only purpose of OTHER is to occupy issue slots on the CPU.
 *   5.  The LD/ST/OTHER traces are obtained by manually or automatically executing real programs.
 * 
 * 
 * Input:  For  n  processors,  you need n  files.   
 * 
 *
 * Timing:   Used  ack event  for invalidations ,  distinguished between owner of invalidation and receiver of invalidations
 *
 * Extensibility:   directly modify the MESICacheLine.java,  also may need to extend some code here
 *
 *
 * Getting Profile/stats:  use  printCache to see cache
 *                        cacheMisses
 *                        execution time is based on  the timestamp that the simulation ends with
 *
 *
 *
 * Note:  may not take into account evictions...
 *
 *
 */

package org.cs533.newprocessor.protocols;

/**
 *
 * @author Vivek
 */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.Scanner;


public class Main
{

    /**
     * @param args the command line arguments
     *
     * Run by typing :  java eventBasedSim [numProcs] [fileNamePrefix]
     *
     */


      public static enum EventType
     {
        ExecuteNextInstruction, BusRead, BusWriteMiss, LineServedRead, LineServedWrite, Invalidate, Update,
        E5, E6, E7, E8
     }
 //profiling
    public static int busTraffic;
    public static int totalReadMisses;
    public static int totalWriteMisses;
    public static int totalBusCycles;
    public static int totalReadAccesses = 0;
    public static int totalWriteAccesses = 0;

    public static int numProcs;
    public static HashMap[] caches;
    public static Scanner[] traceStreams;
    public  static int nextBusAvailable = 0;
    public  static final int busRequestTime = 2;
    public  static final int sendDataTime = 10;
     public static final int busUpdateTime = 8;
    public static int currentTime;
     public static  PriorityQueue<Event> eventQueue;
     public static Event[] pendingBusRequests; //at most one request for each processor
     public static String[] instructionNames   = { "OP", "LD", "ST"}  ;

     //these are the only eventTypes that go on eventqueue
     //E5-E8  are events that can be extended

public static Trace getTrace(int procNum)
{
       if(!traceStreams[procNum].hasNext())
         return null;

       Trace t = new Trace();
             t.instructionType =  Integer.parseInt(traceStreams[procNum].next());
             if(t.instructionType > 0)
             {
                t.address = Integer.parseInt(traceStreams[procNum].next());
             }
            // System.out.println( "Read:  ProcNum  " + procNum + " EventNum : " + t.instructionType + " Address: " + t.address  );

             return t;
}  


public static void printAllCaches()

{
    for (int i = 0; i < numProcs; i++)
    {
        System.out.println("Cache "  +  i +  " : " );
        System.out.println("___________");
        printCache(caches[i]);
        System.out.println("___________");
    }
    
}
public static void printCache(HashMap myCache)
{

    Iterator myIter = myCache.entrySet().iterator();

    for(int i = 0; i < myCache.size();  i++)
    {
       // System.out.println(((CacheLine)myIter.next()).address) ;
        System.out.println( "Entry # " + i + "  is " + ((myCache.entrySet().toArray())[i]).toString() );
       // System.out.println( "Entry # " + i + "  is " + ((MESICacheLine)((myCache.entrySet().toArray())[i])).address );
    }
}

public static void printBlanks(int b)
{
    for(int i = 0; i < b ; i++)
    {
        System.out.print("\t");
    }

}

 public static void main(String[] args) throws FileNotFoundException
    {
         int i = 0;

         numProcs = 2; //read from args[1]
         if(args.length > 1)
         {
             numProcs = Integer.parseInt(args[1]);
         }
         //open one trace stream for each processor
         //store references to streams in an array traceStreams[]
         traceStreams = new Scanner[numProcs];
         String path = "C:\\Users\\Vivek\\Desktop\\dev\\HybridCoherence2\\cs533javasim\\src\\org\\cs533\\newprocessor\\protocols\\traces";
         String testFolderName = "\\test2";
         for(i = 0 ; i< numProcs; i++)
             traceStreams[i] =
                     new Scanner(new BufferedReader(
              new FileReader(path + testFolderName + "\\trace" + i + ".txt"  )));

         //pending request
          pendingBusRequests = new Event[numProcs];
          for(i =0; i< numProcs; i++) pendingBusRequests[i] = null;
         //create one cache for each processor. Each cache is a hashtable of cacheLines

         //caches[i]  stores a reference to cache of processor i
         caches = new HashMap<?,?>[numProcs];
         for (i = 0 ; i< numProcs; i++)
             caches[i] = new HashMap<Integer, FireflyCacheLine>();

       eventQueue = new PriorityQueue<Event>();

         boolean shared;

         //insert initial events , "executeNextTrace" for each processor
          //Scanner s = null;
        for(i = 0; i < numProcs; i++)
        {
           Event e = new Event(0, i, i, Main.EventType.ExecuteNextInstruction);
           eventQueue.offer(e);
        }
         
           Event e;

         printAllCaches();

         while(!eventQueue.isEmpty())
         {
               e = eventQueue.poll();
               currentTime = e.timeStamp;
            //   System.out.println(e.timeStamp + " Proc " + e.procNum + " nextevent :   " +  e.eventType );
              switch(e.eventType)
              {
                  case ExecuteNextInstruction :
                      //execute next instruction for given processor
                      Trace t = getTrace(e.procNum );
                      if(t != null)
                      {
                          System.out.print(e.timeStamp);
                          printBlanks(4*e.procNum);
                          System.out.println( " proc: " + e.procNum + " starting: " + instructionNames[t.instructionType] + " " +  t.address);
                          switch(t.instructionType)
                          {
                              case 0: //non-ld/st instruction
                                e.timeStamp = e.timeStamp + 1;
                                eventQueue.offer(e);
                                break;
                              case 1:  //ld
                               
                                FireflyCacheLine.processorLoad(e, t.address);

                                break;
                              case 2: //store
                                 FireflyCacheLine.processorStore(e, t.address);
                                break;
                          }
                      }
                      break;
                         default:
                             FireflyCacheLine.processBusEvent(e);
                             break;
              }
         }
          System.out.println("Cache Read Miss Rate :  " + totalReadMisses  +   " / "  + totalReadAccesses + " = " +
                     ((double)totalReadMisses)/((double)totalReadAccesses));
          System.out.println("Cache Write Miss Rate :  "  + totalWriteMisses  +   " / "  + totalWriteAccesses + " = "
                  + ((double)totalWriteMisses)/((double)totalWriteAccesses));
           printAllCaches();
   
    }
}