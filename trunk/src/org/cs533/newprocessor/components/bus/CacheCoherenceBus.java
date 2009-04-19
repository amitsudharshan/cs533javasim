/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.bus;

import java.util.ArrayList;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.memorysubsystem.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.L2Cache;

/**
 *
 * @author amit
 */
public class CacheCoherenceBus implements ComponentInterface {

    ArrayList<L1Cache> l1Caches;
    L2Cache l2Cache;
    public static final int LATENCY = 25; //FROM Veenstra/Fowler May 1992

    public void registerCache(L1Cache l1Cache) {
        if (!l1Caches.contains(l1Cache)) {
            l1Caches.add(l1Cache);
        }
    }

    public void runPrep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void runClock() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
