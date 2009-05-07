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

    @Override
    public String toString() {
        return "MESICacheController-"+Integer.toString(cacheID);
    }

    final LRUEvictHashTable<CacheLine<MESILineState>> data;

    public MESICacheController(int cacheID_) {
        super(Logger.getLogger("MESICacheController-"+Integer.toString(cacheID_)));
        cacheID = cacheID_;
        setState(new MESINotReadyState(this));
        data = new LRUEvictHashTable<CacheLine<MESILineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
    }
}
