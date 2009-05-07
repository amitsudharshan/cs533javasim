/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class MESIReadyState extends MESICacheControllerState {
    final MESIBusMessage message;
    final MemoryInstruction pendingRequest;

    MESIReadyState(MESIBusMessage message, MemoryInstruction pendingRequest, MESICacheController controller) {
        super(controller);
        this.message = message;
        this.pendingRequest = pendingRequest;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b.toString()+")");
        return handleBroadcastMessage(b);
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> startTransaction() {
        logger.debug("startTransaction");
        return andJump(message, new MESIRunningState(message, pendingRequest, controller));
    }
}
