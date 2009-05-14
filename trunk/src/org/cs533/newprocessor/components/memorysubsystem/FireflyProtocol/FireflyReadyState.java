/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import java.util.Arrays;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol.FireflyBusMessage.FireflyBusMessageType;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class FireflyReadyState extends FireflyCacheControllerState {

    final MemoryInstruction request;

    FireflyReadyState(MemoryInstruction pendingRequest, FireflyCacheController controller) {
        super(controller);
        this.request = pendingRequest;
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> recieveBroadcastMessage(FireflyBusMessage b) {
        // if this an update and our pending request is a CAS, check
        // if it is possible to fail immediately.
        if (request.getType() == MemoryInstruction.InstructionType.CAS &&
                b.type == FireflyBusMessageType.Update &&
                b.address == request.getInAddress() &&
                !Arrays.equals(b.data, request.getCompareData())) {
            request.setOutData(b.data);
            return andJump(handleBroadcastMessage(b), new FireflyDoneState(request, controller));
        }
        return noJump(handleBroadcastMessage(b));
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> startTransaction() {
        // decide what our message actually should be
        CacheLine<FireflyLineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<FireflyLineState>(request.getInAddress(), null, FireflyLineState.INVALID);
            CacheLine<FireflyLineState> evicted = controller.data.add(line);
            if (evicted != null) {
                FireflyBusMessage message = FireflyBusMessage.DirtyAckWriteback(evicted.address, evicted.data);
                return andJump(message, new FireflyRunningState(message, request, controller));
            }
        }

        return handleClientRequestAsMessage(request, line);
    }

    protected final StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> handleClientRequestAsMessage(MemoryInstruction request, CacheLine<FireflyLineState> line) {
        Either<MemoryInstruction, FireflyBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            // nack acts as transaction done, because it has null aggregator and request
            return andJump(FireflyBusMessage.Done(), new FireflyDoneState(result.first, controller));
        } else {
            return andJump(result.second, new FireflyRunningState(result.second, request, controller));
        }
    }
}
