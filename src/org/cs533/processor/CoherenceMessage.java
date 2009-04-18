/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.processor;

import java.util.BitSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Queue;
/**
 *
 * 
 */
public class CoherenceMessage 
{
    private static int MessageType; //0: write-back
                                     //1: cache-cache 
                                     //2: invalidate 
                                    //3:  update  
                                   
    private  int senderID;// -1:  memory, 0:  Processor 0, 1: processor 1 ...
    private int receiverID; //-1:  memory
    private BitSet MemoryLocation;
    private String data; 
    private int messageID;
    private int status;
    
    public CoherenceMessage(int senderID, int receiverID, int  messageID )
    {
       this.senderID = senderID;
       this.receiverID = receiverID;
       this.messageID = messageID;
    }
    public CoherenceMessage()
    {
        
    }
    
    public int setMessageID(int UniqueMessageID)
    {
        this.messageID = Bus.generateUniqueMessageID();

        return this.messageID;

    }
    public int getSender()
    {
        System.out.println("The sender of message" + messageID + " is " + senderID);
         
        return this.senderID;
    }
   public int getReceiver()
    {
        System.out.println("The receiver of message" + messageID + " is " + senderID);
        return this.senderID;
    }
        
    public BitSet getMemLoc()
    {
         System.out.println("The memory location referenced in message "
                            + messageID + " is " + this.MemoryLocation);
        
        return this.MemoryLocation;
    }
    
    public String getData()
    {
       System.out.println("The content of message" + messageID + " is " + data);
         
        return this.data;
    
    }

    public int getMessageStatus()
    {
          return this.status;
    }

    public int getMessageID() {

        return this.messageID;
    }

}