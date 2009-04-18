/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.processor;
import java.util.LinkedList;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

/**
 *
 * @author Vivek
 */
public class Bus {

    public ArrayList<CoherenceMessage> enRouteMessages;
    private static int unUsedMessageID = 0;
    private static int bandwidth;
    private static int latency;
    public Bus()
    {
     bandwidth = 64; // measured in bits
     latency = 5; //measured in cycles
     enRouteMessages = new ArrayList<CoherenceMessage>();
    }


    //if successful this returns the new size of the list of messages
    // currently on the bus
    public int addMessage(CoherenceMessage e)
    {


     if(enRouteMessages.add(e))
         return enRouteMessages.size();
     else
         return -1;
    }

    public int getMessageStatus(int messageID)
    {
        CoherenceMessage cm = null ;
        if(enRouteMessages.isEmpty())
        {
            System.out.println("no messages on bus.");
           return -1;
        }

        else
        {
         ListIterator<CoherenceMessage> iter =  enRouteMessages.listIterator();
          //not quite sure how to use
         if( iter.next() != null)
           {
             cm = iter.previous();
             if(cm.getMessageID() == messageID)
                 return cm.getMessageStatus();

           }

        while(iter.hasNext())
          {
              cm = iter.next();
             if(cm.getMessageID() == messageID)
                return cm.getMessageStatus();
          }
        }

        return -1;
    }

    public static int generateUniqueMessageID()
    {
        int myID = unUsedMessageID;
        unUsedMessageID++;
        return myID;
    }

    public ArrayList getBusSnapshot()
    {
      ArrayList<CoherenceMessage> enRouteMessagesSnapshot;
      enRouteMessagesSnapshot = this.enRouteMessages;
      return enRouteMessagesSnapshot;
    }

    public boolean isAvailable()
      {
        if(enRouteMessages.isEmpty())
        {
            System.out.println("The bus is available.");
            return true;
        }
        else
        {
            System.out.println("The bus is not available.");
            return false;
        }


      }

    //arbitration  when many messages need the bus at the same time
    //
    public boolean performBusArbitration()
    {

        return false;
    }
}
