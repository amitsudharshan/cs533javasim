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
public class HybridReadyState extends HybridCacheControllerState {
    final HybridBusMessage message;
    final MemoryInstruction pendingRequest;

    HybridReadyState(HybridBusMessage message, MemoryInstruction pendingRequest, HybridCacheController controller) {
        super(controller);
        this.message = message;
        this.pendingRequest = pendingRequest;
    }

    @Override
    public StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> recieveBroadcastMessage(HybridBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b.toString()+")");
        return handleBroadcastMessage(b);
    }

    @Override
    public StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> startTransaction() {
        logger.debug("startTransaction");
        return andJump(message, new HybridRunningState(message, pendingRequest, controller));
    }
}
