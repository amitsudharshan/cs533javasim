/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.cs533.newprocessor.components.memorysubsystem.L1MESICacheLine;
import org.cs533.newprocessor.components.memorysubsystem.L1MESICacheLine.Event;
/**
 *
 * @author Vivek
 */

public  class L1_MESI_Test
{
    L1MESICacheLine myL1Cache = new L1MESICacheLine();
    Random rand = new Random();
    LRUEvictHashTable<CacheLine> L1Lines =  new LRUEvictHashTable(10);

  public L1_MESI_Test(int data)
  {
    L1MESICacheLine newCacheLine =  null;
    for (int i = 0; i<10; i++)
    {
     newCacheLine = new L1MESICacheLine((rand.nextInt()%20), false);
    L1Lines.add(newCacheLine);
    }
      if(newCacheLine != null)
         System.out.println(newCacheLine.toString());



  //do many differnt instructions at this...\
      for (int i = 0; i<10; i++)
    {
     System.out.println("creating new cache lines");
     newCacheLine = new L1MESICacheLine((rand.nextInt()%90), false);
    L1Lines.add(newCacheLine);
    if(newCacheLine != null)
    {
       
         System.out.println(newCacheLine.toString());
    }
    else
     {
      System.out.println("cache line was null!");
     }
    }

     System.out.println("created all the cache lines successfully");
 

    
     L1MESICacheLine x = null;
     L1MESICacheLine[] myLines =  (L1MESICacheLine[]) L1Lines.values().toArray();
     Iterator iter = L1Lines.values().iterator();
     System.out.println("beginning simulated events on state machine");
     int i = 0;
   
         for(i = 0; i< myLines.length; i++)
         {
         System.out.println("Simulation: On the iteration " + i  );
        //models a producer-consumer pattern

        myLines[i].onMessage(2, Event.BusRead, 0, false, data);
        myLines[i].onMessage(3, Event.PWrite, 0, true, data);
        myLines[i].onMessage(1, Event.PWrite, 0, false, data);
        myLines[i].onMessage(1, Event.PWrite, 0, false, data);
        myLines[i].onMessage(1, Event.PWrite, 0, false, data);
        myLines[i].onMessage(1, Event.PWrite, 0, false, data);
        myLines[i].onMessage(1, Event.PWrite, 0, false, data);
        myLines[i].onMessage(1, Event.BusRead, 0, false, data);
        myLines[i].onMessage(3, Event.PWrite, 0, true, data);
        myLines[i].onMessage(3, Event.BusInvalidate, 0, true, data);
        myLines[i].onMessage(4, Event.BusInvalidate, 0, false, data);
        System.out.println("ending iteration"+ i);
         }
     for(i = 0; i< myLines.length; i++)
      {  
        System.out.println("The "+ i +  " th iteration gives" + myLines[i].toString());
      }
  }


  public static void main(String[] args)
  {
      System.out.println("beginning test for L1_MESI_Test");
      L1_MESI_Test testL1 = new L1_MESI_Test(17);
      System.out.println("completed test");
  }
 }