/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author Vivek
 */
public interface AbstractCacheCoherenceProtocol
{


  /*
        <Event, StateMachine>

    Event
    StateMachine {
     processEvent(Event);
}
*/



    public int BusRead();
    public int BusWrite();
    public int ProcessorRead();
    public int ProcessorWrite();
    public int BusWriteMiss();
    public int BusReadMiss();
    public int ProcessorReadMiss();
    public int ProcessorWriteMiss();
    public int BusInvalidate();


    static int currentState;
    static boolean sharedLine;


}
