/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class MESIReadyState extends MESICacheControllerState {

    final MESIBusMessage message;
    final MemoryInstruction pendingRequest;

    @Override
    public String toString() {
        return "MESIReadyState(" + message.toString() + "," + pendingRequest.toString() + ")";
    }

    MESIReadyState(MESIBusMessage message, MemoryInstruction pendingRequest, MESICacheController controller) {
        super(controller);
        assert message != null;
        assert pendingRequest != null;
        this.message = message;
        this.pendingRequest = pendingRequest;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        MESIBusMessage reply = handleBroadcastMessage(b);
        if (b.address != pendingRequest.getInAddress()) {
            return noJump(reply);
        } else {
            // it might change how we need to handle a request.
            // In particular we could be ready to issue an Invalidate
            // presuming the line is shared, but then have a bus
            // message invalidate our line
            CacheLine<MESILineState> line = controller.data.get(pendingRequest.getInAddress());
            Either<MemoryInstruction, MESIBusMessage> newAction = handleClientRequest(pendingRequest, line);
            if (newAction.isFirst) {
                // somehow, we can finish the request right away
                // (I don't think this is possible in MESI
                return andJump(reply, new MESIDoneState(pendingRequest, controller));
            } else {
                // still need to wait, but perhaps with a different pending BusMessage
                return andJump(reply, new MESIReadyState(newAction.second, pendingRequest, controller));
            }
        }
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> startTransaction() {
        return andJump(message, new MESIRunningState(message, pendingRequest, controller));
    }
}
