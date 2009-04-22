/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.core.ProcessorCore;

/**
 *
 *
 */
public class L1Cache implements ComponentInterface {

    ProcessorCore core;
    CacheCoherenceBus bus;
    public static final int LATENCY = 1;

    public L1Cache(ProcessorCore _core, CacheCoherenceBus _bus) {
        bus = _bus;
        core = _core;
        bus.registerCache(this);
    }

    public void runPrep()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void runClock() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLatency() {
        return LATENCY;
    }
}
