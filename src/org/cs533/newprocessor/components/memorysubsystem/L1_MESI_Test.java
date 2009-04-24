/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

import java.util.Collections;
import java.util.LinkedList;
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
   int  i = 0;
    Random rand = new Random();
    int[] x = new int[4];



     LRUEvictHashTable<L1MESICacheLine> L1Lines1
     = new LRUEvictHashTable<L1MESICacheLine>(10);

     LRUEvictHashTable<L1MESICacheLine> L1Lines2
     = new LRUEvictHashTable<L1MESICacheLine>(10);


     LRUEvictHashTable<L1MESICacheLine> L1Lines3
     = new LRUEvictHashTable<L1MESICacheLine>(10);


     LRUEvictHashTable<L1MESICacheLine> L1Lines4
     = new LRUEvictHashTable<L1MESICacheLine>(10);
    //   new LRUEvictHashTable<L1MESICacheLine>();
      
  public L1_MESI_Test(int data)
  {
    L1MESICacheLine newCacheLine =  null;
    for (int i = 0; i<10; i++)
    {
     newCacheLine = new L1MESICacheLine((rand.nextInt()%20), false);
    L1Lines1.add(newCacheLine);
    newCacheLine = new L1MESICacheLine((rand.nextInt()%20), false);
     L1Lines2.add(newCacheLine);
    newCacheLine = new L1MESICacheLine((rand.nextInt()%20), false);
     L1Lines3.add(newCacheLine);
    newCacheLine = new L1MESICacheLine((rand.nextInt()%20), false);
     L1Lines4.add(newCacheLine);
    }

      int data2 = 7;
    int data3 = 11;
    int data4 = 13;
    int data5 = 17;
    int data6 = 19;
    for (int i = 0; i<10; i++)
      {
     System.out.println("creating new cache lines");
     newCacheLine = new L1MESICacheLine((rand.nextInt()%90), false);
     L1Lines1.add(newCacheLine);
     newCacheLine = new L1MESICacheLine((rand.nextInt()%90), false);
     L1Lines2.add(newCacheLine);
     newCacheLine = new L1MESICacheLine((rand.nextInt()%90), false);
     L1Lines3.add(newCacheLine);
     newCacheLine = new L1MESICacheLine((rand.nextInt()%90), false);
     L1Lines4.add(newCacheLine);

      }

  /*
   *
   * P1 read  0x100
   * P2 read 0x100   (should become shared in both)
   * P1 write 0x100 (invalidate P2's copy)
   * P2 reads 0x100  (back to shared in both)
   * P2 write 0x100  (invalidate P1's copy ,  share P2)
   */
    
   Iterator iter =  L1Lines1.values().iterator();
     Iterator iter2 =  L1Lines2.values().iterator();
  
    L1MESICacheLine x = new L1MESICacheLine();
    L1MESICacheLine y = new L1MESICacheLine();
    x = (L1MESICacheLine) iter.next();
    y = (L1MESICacheLine) iter2.next();
    x.address = 100;
    y.address = 100;
    x.onMessage(Event.PRead, 6);
    y.onMessage(Event.BusRead, 6);
    x.onMessage(Event.PWrite, 7);
    y.onMessage(Event.BusWrite, 7);
    System.out.println("The two lines should be ocherent: 1st cache line"+ x.data +"2nd cache line" + y.data);

    x.onMessage(Event.PWrite, 8);
    y.onMessage(Event.BusWrite, 8);
    y.onMessage(Event.PRead, 8);
      System.out.println("The two lines should be ocherent: 1st cache line"+ x.data +"2nd cache line" + y.data);

    x.onMessage(Event.BusRead, 8);
    y.onMessage(Event.PWrite, 9);
    x.onMessage(Event.BusWrite, 9);

    System.out.println("The two lines should be ocherent: 1st cache line:" 
            + x.getData()
            + "2nd cache line"
            + y.getData());

/*
    L1MESICacheLine[] myLines1 = new L1MESICacheLine[10];
    L1MESICacheLine[] myLines2 = new L1MESICacheLine[10];
    L1MESICacheLine[] myLines3 = new L1MESICacheLine[10];
    L1MESICacheLine[] myLines4 = new L1MESICacheLine[10];

    for(int i = 0 ;  i <  10; i++)
    {
        myLines1[i] = new L1MESICacheLine((rand.nextInt()%90), false);
        myLines2[i] = new L1MESICacheLine((rand.nextInt()%90), false);
        myLines3[i] = new L1MESICacheLine((rand.nextInt()%90), false);
        myLines4[i] = new L1MESICacheLine((rand.nextInt()%90), false);
    }
     L1MESICacheLine x = null;
     //L1MESICacheLine[] myLines =  (L1MESICacheLine[]) L1Lines.values().toArray();
     // myLines = L1Lines.values().toArray(myLines);

     //Iterator iter = L1Lines1.values().iterator();
     //System.out.println("beginning simulated events on state machine");
     int i = 0;
 
         for(i = 0; i< myLines1.length; i++)
         {
         //System.out.println("Simulation: On the iteration " + i  );
        //models a producer-consumer pattern
        if(myLines1[i] != null)
        {
        myLines2[i].onMessage(2, Event.BusRead, 0, false, data);
        System.out.println("Beginning series of writes...");
        myLines2[i].onMessage(3, Event.PWrite, 0, true, data2);
        myLines2[i].onMessage(1, Event.PWrite, 0, false, data3);
        myLines2[i].onMessage(1, Event.PWrite, 0, false, data4);
        myLines2[i].onMessage(1, Event.PWrite, 0, false, data5);
        myLines2[i].onMessage(1, Event.PWrite, 0, false, data6);
        myLines2[i].onMessage(1, Event.PWrite, 0, false, data6);
        System.out.println("ending series of writes..");
        myLines2[i].onMessage(1, Event.BusRead, 0, false, data);
        myLines2[i].onMessage(3, Event.PWrite, 0, true, data6);
        System.out.println("The data in cache is " +  myLines[i].data);
        myLines2[i].onMessage(3, Event.BusInvalidate, 0, true, data6);
        response = myLines[i].onMessage(4, Event.BusInvalidate, 0, false, data6);
        }
             System.out.println("ending iteration"+ i);
         }




     for(i = 0; i< myLines1.length; i++)
      {  
        //System.out.println("The "+ i +  " th iteration gives" + myLines[i].toString());
        System.out.println("Data in cache line " + i+ " ----> " + myLines1[i].getData());
       }

 */
  }



  public static void main(String[] args)
  {
      System.out.println("beginning test for L1_MESI_Test");
      L1_MESI_Test testL1 = new L1_MESI_Test(17);
      System.out.println("completed test");
  }
 }
