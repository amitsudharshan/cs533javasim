/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hybridcachecoherence;

/**
 *
 * @author Vivek
 */

import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.util.ArrayList;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
           java.io.BufferedReader
            BufferedReader bf1 = new BufferedReader(new FileReader("instructionStream1.in"));
             BufferedReader bf2 = new BufferedReader(new FileReader("instructionStream2.in"));
           ArrayList<Core> system = new ArrayList<Core>();
            Bus bus = new Bus();

        //get input files that indicate instruction stream for each processor
        // note that we process instructions offline rather than online
        for (int i = 0; i < args.length; i++)
        {
          //store complete instruction stream into some data structure



          storeInstructionsInMemory();
          system[i].addInstructionsToBuffer();

        }



       for(int j= 0; j < NumCycles; j++)
       {

           for(i = 0; i < numProcessors; i++)
           {

              //for each processor(round-robin) to check for new instruction

              //issueNextInstruction attempts to issue a new instruction
              //from processor's instruction queue
              //doing this increments PC

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

}
