/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.processor;

/**
 *
 * @author Vivek
 */

import hybridcachecoherence.*;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.util.ArrayList;

public class Main {

    /**
     * @param args the command line arguments
     */

    public static final int ALUExecutionTime = 1; //measured in cycles
    public static final int LD_ExecutionTime = 5; //measured in cycles
    public static final int ST_ExecutionTime = 5; //measured in cycles



    public static void main(String[] args) {
       
        // TODO code application logic here

           for( int i = 0 ;   i < 0;  i++)
           {
            BufferedReader bf1 = new BufferedReader(new FileReader("instructionStream1.in"));
            BufferedReader bf2 = new BufferedReader(new FileReader("instructionStream2.in"));

            ArrayList<Processor> system = new ArrayList<Processor>();
            Bus bus = new Bus();
           }

     //create EventQueue

            //initialize
          for(int p = 0 ; p <  NumProcessors; p++)
          {
                  //create input trace
                  instrStream[p] = new BufferedStream(readFromFile());//for processor i
                  Event e = new Event(0 + ExecutionTime);
                  eventQueue.add(e);
          }

      while(1)
       {
          Event e = eventQueue.remove();
          switch(e.EventType)
                  case executeTrace :
                      ExecuteEvent e1 = (ExecuteEvent)e;
                      Trace t = instrStream[e1.procNum].getNextTrace();
                      switch ( t.TraceType )
                      {
                          case 0: //ALU op
                                 ExecutionEvent e2 = new ExecutionEvent(e.time + ALUExecutionTime);
                                 eventQueue.enqueue(e2);

                                 break;
                          case 1: //LD
                               ExecuteEvent e2 = new ExecutionEvent(e.time + LDExecutionTime);
                               eventQueue.enqueue(e2);

                              break;

                          case 2:  //ST
                               ExecuteEvent e2 = new ExecutionEvent(e.time + LDExecutionTime);
                               eventQueue.enqueue(e2);

                              break;
                      }

                  case busTrace:
                      BusEvent be = (BusEvent) e;

                      ExecuteEvent e1 = (ExecuteEvent)e;
                      Trace t = instrStream[e1.procNum].getNextTrace();

              //for each processor(round-robin) to check for new instruction

             //issueNextInstruction attempts to issue a new instruction
              //from processor's instruction queue
              //doing this increments PC




        //get input files that indicate instruction stream for each processor
        // note that we process instructions offline rather than online
        for (int i = 0; i < args.length; i++)
        {
          //store complete instruction stream into some data structure

          storeInstructionsInMemory();
          system[i].addInstructionsToBuffer();

        }

             Instruction instr = system[i].getNextInstruction();

             if(system[i].isFree())//check is processor is free
              {
                  system[i].issueInstruction(instr);//removes from instruction queue
                  system[i].incrementProgramCounter();

                  //Perform necessary coherence operation based on instruction

                  if(instr.getType() == "ProcessorWrite")
                  {
                       CacheController cc = system[i].getCache().getElement(instr.getMemLocOfData);
                       cc.transition("pwrite");//cc refers to the shared cache line
                       if(cc.shared == 1)
                       {
                       CoherenceMessage cm = new CoherenceMessage();
                       cm.setSender(i);
                       cm.setReceiver("ALL");//broadcast
                       cm.MessageContent("Invalidate line X");
                       if(bus.isAvailable())
                           bus.addMessage(cm);
                       }
                  }

                  else if(instr.getType() == "ProcessorRead")
                  {
                      CacheController cc = system[i].getCache().getElement(instr.getMemLocOfData);
                       cc.transition("busRead");//cc refers to the shared cache line
                       if(cc.shared == 0)
                       {
                         CoherenceMessage cm = new CoherenceMessage();
                         cm.setSender(i);
                         cm.setReceiver("Anyone");//whoever has the line
                         cm.MessageContent("if you have line X in your cache" +
                                 "send me back a current copy of line X on the bus");
                         if(bus.isAvailable())
                            bus.addMessage(cm);
                       }

                  }

                  //...and so on



              }


           }



       }


    }
*/
        
}
}
