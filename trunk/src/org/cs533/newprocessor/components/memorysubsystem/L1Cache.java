/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;

/**
 *
 * @author amit
 */
public class L1Cache implements ComponentInterface {

    public L1Cache(CacheCoherenceBus bus) {
        bus.registerCache(this);
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
