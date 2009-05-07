/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import org.cs533.newprocessor.components.memorysubsystem.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.Globals;

/**
 *
 * @author amit
 */
public class HybridCacheController extends CacheController<HybridBusMessage> {

    int cacheID;

    @Override
    public String toString() {
        return "HybridCacheController-"+Integer.toString(cacheID);
    }

    final LRUEvictHashTable<CacheLine<HybridLineState>> data;

    public HybridCacheController(int cacheID_) {
        super(Logger.getLogger("HybridCacheController-"+Integer.toString(cacheID_)));
        cacheID = cacheID_;
        setState(new HybridNotReadyState(this));
        data = new LRUEvictHashTable<CacheLine<HybridLineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
    }
}
