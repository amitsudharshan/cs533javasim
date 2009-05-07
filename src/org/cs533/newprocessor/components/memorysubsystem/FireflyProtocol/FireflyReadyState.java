/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import java.util.Arrays;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol.FireflyBusMessage.FireflyBusMessageType;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class FireflyReadyState extends FireflyCacheControllerState {
    final FireflyBusMessage message;
    final MemoryInstruction pendingRequest;

    FireflyReadyState(FireflyBusMessage message, MemoryInstruction pendingRequest, FireflyCacheController controller) {
        super(controller);
        this.message = message;
        this.pendingRequest = pendingRequest;
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> recieveBroadcastMessage(FireflyBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b.toString()+")");
        // check if this is an update that will make our pending CAS fail.
        // if we evicted a line we might as well wait till we get the bus for that,
        // and rely on handleClientRequest to decide we are done as soon as
        // the writeback comes back
        if (pendingRequest.getType() == MemoryInstruction.InstructionType.CAS &&
                message.type == FireflyBusMessageType.Update &&
                b.type == FireflyBusMessageType.Update &&
                b.address == pendingRequest.getInAddress() &&
                !Arrays.equals(b.data, pendingRequest.getCompareData()))
        {
            CacheLine<FireflyLineState> line = controller.data.get(b.address);
            line.data = b.data;
            line.state = FireflyLineState.SHARED;
            pendingRequest.setOutData(line.data);
            return andJump(FireflyBusMessage.Ack(), new FireflyDoneState(pendingRequest, controller));
        }
        return handleBroadcastMessage(b);
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> startTransaction() {
        logger.debug("startTransaction");
        return andJump(message, new FireflyRunningState(message, pendingRequest, controller));
    }
}
