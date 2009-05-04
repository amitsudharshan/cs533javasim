/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.memorysubsystem.*;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MESIProtocol.MESIBusMessage;

/**
 *
 * @author amit
 */
public class MESICacheController extends CacheController<MESIBusMessage> {

    static Logger logger = Logger.getLogger(L1Cache.class.getName());
    int cacheID;


    final LRUEvictHashTable<CacheLine<MESILineState>> data;

    MESICacheController(int cacheID_) {
        super();
        cacheID = cacheID_;
        data = new LRUEvictHashTable<CacheLine<MESILineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
        state = new MESINotReadyState(this);
    }
}
