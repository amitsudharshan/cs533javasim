/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.memorysubsystem.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.Globals;

/**
 *
 * @author amit
 */
public class FireflyCacheController extends CacheController<FireflyBusMessage> {

    int cacheID;

    @Override
    public String toString() {
        return "FireflyCacheController."+Integer.toString(cacheID);
    }

    final LRUEvictHashTable<CacheLine<FireflyLineState>> data;

    public FireflyCacheController(int cacheID_) {
        super(Logger.getLogger("CacheController.Firefly."+Integer.toString(cacheID_)));
        cacheID = cacheID_;
        setState(new FireflyNotReadyState(this));
        data = new LRUEvictHashTable<CacheLine<FireflyLineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
    }
}
