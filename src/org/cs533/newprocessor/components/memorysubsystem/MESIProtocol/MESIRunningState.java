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
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

/**
 * We own the bus and are waiting for a reply.
 * @author amit
 */
public class MESIRunningState extends MESICacheControllerState {

    MESIBusMessage currentRound;
    final MemoryInstruction pendingRequest;

    @Override
    public String toString() {
        return "MESIRunningState("+currentRound.toString()+","+pendingRequest.toString()+")";
    }

    public MESIRunningState(MESIBusMessage currentRound, MemoryInstruction pendingRequest, MESICacheController controller) {
        super(controller);
        this.currentRound = currentRound;
        this.pendingRequest = pendingRequest;
        assert controller.dirtyWritebackAddr == -1;
    }

    protected final StateAnd<MESIBusMessage,CacheControllerState<MESIBusMessage>> handleClientRequestAsMessage(MemoryInstruction request, CacheLine<MESILineState> line) {
        Either<MemoryInstruction,MESIBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            // nack acts as transaction done, because it has null aggregator and request
            return andJump(MESIBusMessage.Done(), new MESIDoneState(result.first, controller));
        } else {
            currentRound = result.second;
            assert currentRound != null;
            return noJump(result.second);
        }
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveMemoryResponse(MemoryInstruction response) {
        if (response.getType() == InstructionType.Load) {
            // must have been handling a Nack on a cache-to-cache get.
            assert response.getInAddress() == pendingRequest.getInAddress();
            CacheLine<MESILineState> line = controller.data.get(pendingRequest.getInAddress());
            if (line == null) {
                line = new CacheLine<MESILineState>(response, MESILineState.EXCLUSIVE);
                controller.data.add(line);
            }
            line.state = MESILineState.EXCLUSIVE;
            line.data = response.getOutData();
            return handleClientRequestAsMessage(pendingRequest, line);
        } else {
            // Either we were writing back a dirty-acked line we got from another client
            // (AckDirty case in receiveBusResponse), or evicting a line prior to even
            // beginning our transaction (MESINotReadyState.recieveClientRequest).
            // In both cases we set up the line state ahead of time, so just do our request.
            return handleClientRequestAsMessage(pendingRequest, controller.data.get(pendingRequest.getInAddress()));
        }
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> receiveBusResponse(MESIBusMessage b) {
        CacheLine<MESILineState> line;
        // must have been a cache to cache round requiring a response: Get or GetX
        switch (b.type) {
            case Nack:
                // getting from memory - line will be exclusive.
                currentRound = MESIBusMessage.Load(pendingRequest.getInAddress());
                return noJump(currentRound);
            case AckData:
                // got from other cache, go Shared / Exclusive as Get specified
                line = controller.data.get(pendingRequest.getInAddress());
                line.data = b.data;
                switch (currentRound.type) {
                    case Get:
                        line.state = MESILineState.SHARED;
                        break;
                    case GetX:
                        line.state = MESILineState.EXCLUSIVE;
                        break;
                    default:
                        logger.fatal(("Apparently got ack in response to no-reply request type " + currentRound.type.toString()));
                        throw new RuntimeException();
                }
                return handleClientRequestAsMessage(pendingRequest, line);
            case AckDirty:
                // got line from other cache, store it in ours as above,
                // but arrange to writeback before we process
                line = controller.data.get(pendingRequest.getInAddress());
                line.data = b.data;
                switch (currentRound.type) {
                    case Get:
                        line.state = MESILineState.SHARED;
                        break;
                    case GetX:
                        line.state = MESILineState.EXCLUSIVE;
                        break;
                    default:
                        logger.fatal(("Apparently got ack in response to no-reply request type " + currentRound.type.toString()));
                        throw new RuntimeException();
                }
                currentRound = MESIBusMessage.Writeback(pendingRequest.getInAddress(), b.data);
                return noJump(currentRound);
            default:
                logger.fatal("Unexpected bus response type " + b.type.toString());
                throw new RuntimeException();
        }
    }
}
