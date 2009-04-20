/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import org.cs533.newprocessor.components.memorysubsystem.*;
import org.cs533.newprocessor.components.memorysubsystem.l2cache.FullyAssociativeCache;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;

/**
 *
 * @author amit
 */
public class L1Cache extends FullyAssociativeCache {

    MemoryInterface parentMemory;
    CacheCoherenceBus bus;

    public L1Cache(CacheCoherenceBus bus_, MemoryInterface parentMemory_) {
        super(Globals.L1_SIZE_IN_NUMBER_OF_LINES, Globals.L2_CACHE_LATENCY, parentMemory_);
        bus = bus_;
        bus.registerCache(this);
    }
}
