/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.memorysubsystem.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.Globals;

/**
 *
 * @author amit
 */
public class MESICacheController extends CacheController<MESIBusMessage> {

    int cacheID;
    public int dirtyWritebackAddr = -1;
    //used by client states to not be surprsied by the Writeback when their
    //line is in the shared state, when they were the one that just provided the line

    @Override
    public String toString() {
        return "MESICacheController."+Integer.toString(cacheID);
    }

    final LRUEvictHashTable<CacheLine<MESILineState>> data;

    public MESICacheController(int cacheID_) {
        super(Logger.getLogger("CacheController.MESI."+Integer.toString(cacheID_)));
        cacheID = cacheID_;
        setState(new MESINotReadyState(this));
        data = new LRUEvictHashTable<CacheLine<MESILineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
    }
}
