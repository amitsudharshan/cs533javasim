/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.protocols;
import java.lang.Comparable;

/**
 *
 * @author Vivek
 */
public class Event implements Comparable {

    protected int timeStamp;
    protected int source;
    public int procNum;
    public Main.EventType eventType;
    public int address; // this identifies a cache line for
                        // an event coming from the bus

    public Event(int timestamp, int source, int _procNum, Main.EventType _eventType)
    {
        this.timeStamp = timestamp;
        this.source = source;
        procNum = _procNum;
        eventType  = _eventType;
    }

    public Event(int timestamp)
    {
        this.timeStamp = timestamp;
    }


    public Event ()
    {
        this.timeStamp = 0;
        this.source = 0;
    }

    public int compareTo(Object o)
    {
       Event e = (Event)o;
       if(timeStamp < e.timeStamp)
           return -1;
       else if (timeStamp == e.timeStamp)
           return 0;
       else
           return 1;
    }

    public int getTimeStamp()
    {
        System.out.println("Event.java: getTimeStamp()  returned " + this.timeStamp);
        return this.timeStamp;
    }
}
