/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class HybridDoneState extends HybridCacheControllerState {
    MemoryInstruction response;

    public HybridDoneState(MemoryInstruction response, HybridCacheController controller) {
        super(controller);
        this.response = response;
    }

    @Override
    public StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> recieveBroadcastMessage(HybridBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b+")");
        StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> action = handleBroadcastMessage(b);
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<HybridBusMessage>> pollRequestStatus(MemoryInstruction request) {
        logger.debug("pollRequestStatus");
        return andJump(response, new HybridNotReadyState(controller));
    }
}
