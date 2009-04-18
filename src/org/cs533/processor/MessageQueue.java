/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.processor;

import java.util.Queue;


/**
 *
 * This is the per-cache message queue for each node
 */


public class MessageQueue
{

    private Queue<CoherenceMessage> messages;

    public CoherenceMessage issueMessage()
    {
       if(messages.isEmpty())
            return null;


      CoherenceMessage cm = new CoherenceMessage();


       while((cm = messages.poll()) != null)
       {
          cm.setMessageID(Bus.generateUniqueMessageID());
       }

      return cm;

    }

}

