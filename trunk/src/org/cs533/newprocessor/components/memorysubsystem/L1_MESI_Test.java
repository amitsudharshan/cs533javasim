/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.cs533.newprocessor.components.memorysubsystem.L1MESI.Event;
/**
 *
 * @author Vivek
 */
public  class L1_MESI_Test
{

    L1Cache myL1Cache = new L1Cache(null, null);
    Random rand = new Random();
    LRUEvictHashTable<CacheLine> L1Lines =  new LRUEvictHashTable(10);
 
  public L1_MESI_Test(int data)
  {
      L1MESI newCacheLine =  null;
    for (int i = 0; i<10; i++)
    {
     newCacheLine = new L1MESI((rand.nextInt()%20), false) ;
    L1Lines.add(newCacheLine);
    }
      if(newCacheLine != null)
         System.out.println(newCacheLine.toString());



  //do many differnt instructions at this...\
      for (int i = 0; i<10; i++)
    {
     newCacheLine = new L1MESI((rand.nextInt()%90), false) ;
    L1Lines.add(newCacheLine);
    if(newCacheLine != null)
         System.out.println(newCacheLine.toString());
    }

      Iterator iter = L1Lines.values().iterator();
      while(iter.hasNext())
      {
        L1MESI x = (L1MESI) L1Lines.values().iterator().next();
        x.onMessage(2, Event.BusRead, 0, false, data);
        x.onMessage(3, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.PWrite, 0, false, data);
        x.onMessage(1, Event.BusRead, 0, false, data);
        x.onMessage(3, Event.PWrite, 0, false, data);
        x.onMessage(3, Event.BusInvalidate, 0, false, data);
        x.onMessage(4, Event.BusInvalidate, 0, false, data);

      }

      Iterator iter2 = L1Lines.values().iterator();
     L1MESI y = (L1MESI) iter2.next();
     int counter = 1;
      while(iter.hasNext())
      {
         y = (L1MESI) iter2.next();
        System.out.println("The "+ counter +  " th iteration gives" + y.toString());
        

      }
  }

}

