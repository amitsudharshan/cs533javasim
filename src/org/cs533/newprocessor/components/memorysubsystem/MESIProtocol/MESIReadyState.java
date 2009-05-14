/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MESIProtocol.MESIBusMessage.MESIBusMessageType;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class MESIReadyState extends MESICacheControllerState {

    final MemoryInstruction request;

    @Override
    public String toString() {
        return "MESIReadyState(" + request.toString() + ")";
    }

    MESIReadyState(MemoryInstruction pendingRequest, MESICacheController controller) {
        super(controller);
        assert pendingRequest != null;
        this.request = pendingRequest;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        return noJump(handleBroadcastMessage(b));
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> startTransaction() {
        // decide what our message actually should be
        CacheLine<MESILineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<MESILineState>(request.getInAddress(), null, MESILineState.INVALID);
            CacheLine<MESILineState> evicted = controller.data.add(line);
            if (evicted != null) {
                MESIBusMessage message = MESIBusMessage.Writeback(evicted.address, evicted.data);
                return andJump(message, new MESIRunningState(message, request, controller));
            }
        }
        return handleClientRequestAsMessage(request, line);
    }

    protected final StateAnd<MESIBusMessage,CacheControllerState<MESIBusMessage>> handleClientRequestAsMessage(MemoryInstruction request, CacheLine<MESILineState> line) {
        Either<MemoryInstruction,MESIBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            // nack acts as transaction done, because it has null aggregator and request
            return andJump(MESIBusMessage.Done(), new MESIDoneState(result.first, controller));
        } else {
            return andJump(result.second, new MESIRunningState(result.second, request, controller));
        }
    }
}
