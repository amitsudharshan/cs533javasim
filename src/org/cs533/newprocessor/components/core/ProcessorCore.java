/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.core;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.core.pipeline.Pipeline;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.l1cache.bus.CacheCoherenceBus;

/**
 *
 * @author amit
 */
public class ProcessorCore implements ComponentInterface {

    public static final int LATENCY = 1;
    public int startPC;
    public int endPC;
    L1Cache lCache;
    Pipeline p;

    public ProcessorCore(int _startPC, int _endPC,CacheCoherenceBus bus) {
        startPC = _startPC;
        endPC = _endPC;
    //    lCache = new L1Cache(this,bus);
        p = new Pipeline();
    }

    public void runPrep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void runClock() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLatency() {
        return LATENCY;
    }

}
