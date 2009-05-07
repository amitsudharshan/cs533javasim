/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol.FireflyBusMessage.FireflyBusMessageType;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.InstructionType;

/**
 * We own the bus and are waiting for a reply.
 * @author amit
 */
public class FireflyRunningState extends FireflyCacheControllerState {

    FireflyBusMessage currentRound;
    final MemoryInstruction pendingRequest;

    public FireflyRunningState(FireflyBusMessage currentRound, MemoryInstruction pendingRequest, FireflyCacheController controller) {
        super(controller);
        this.currentRound = currentRound;
        this.pendingRequest = pendingRequest;
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> recieveMemoryResponse(MemoryInstruction response) {
        logger.debug("recieveMemoryResponse("+response.toString()+","+currentRound.toString()+")");
        if (response.getType() == InstructionType.Load) {
            // must have been handling a Nack on a cache-to-cache get.
            assert response.getInAddress() == pendingRequest.getInAddress();
            CacheLine<FireflyLineState> line = controller.data.get(pendingRequest.getInAddress());
            if (line == null) {
                line = new CacheLine<FireflyLineState>(response, FireflyLineState.EXCLUSIVE);
                controller.data.add(line);
            }
            line.state = FireflyLineState.EXCLUSIVE;
            line.data = response.getOutData();
            Either<MemoryInstruction,FireflyBusMessage> result = handleClientRequest(pendingRequest, line);
            if (result.isFirst) {
                return andJump(FireflyBusMessage.Done(), new FireflyDoneState(result.first, controller));
            } else {
                currentRound = result.second;
                return noJump(currentRound);
            }
        } else {
            assert response.getType() == InstructionType.Store;
            if (currentRound.type == FireflyBusMessageType.FinalWriteback) {
                // we are done.
                return andJump(FireflyBusMessage.Done(), new FireflyDoneState(pendingRequest, controller));
            } else {
                if (currentRound.getMemoryRequest() == null ||
                        currentRound.getMemoryRequest().getType() != InstructionType.Store
                       || currentRound.type != FireflyBusMessageType.DirtyAckWriteback) {
                    assert currentRound.getMemoryRequest().getType() == InstructionType.Store;
                    assert currentRound.type == FireflyBusMessageType.DirtyAckWriteback;
                }
                Either<MemoryInstruction,FireflyBusMessage> result = handleClientRequest(pendingRequest, controller.data.get(pendingRequest.getInAddress()));
                if (result.isFirst) {
                    return andJump(FireflyBusMessage.Done(), new FireflyDoneState(result.first, controller));
                } else {
                    currentRound = result.second;
                    return noJump(currentRound);
                }
            }
        }
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> receiveBusResponse(FireflyBusMessage b) {
        logger.debug("recieveBusResponse("+b.toString()+","+currentRound.toString()+")");
        CacheLine<FireflyLineState> line;
        // must have been a cache to cache round requiring a response: Get or GetX
        switch (b.type) {
            case Nack:
                if (currentRound.type == FireflyBusMessageType.Update) {
                    // we can go modified, nobody else had the line
                    // FIXME and line was shared?
                    line = controller.data.get(pendingRequest.getInAddress());
                    line.state = FireflyLineState.MODIFIED;
                    line.data = currentRound.data;
                    return andJump(FireflyBusMessage.Done(), new FireflyDoneState(pendingRequest, controller));
                } else {
                    // was trying to get data, need to get from memory, line will end up exclusive
                    currentRound = FireflyBusMessage.Load(pendingRequest.getInAddress());
                    return noJump(currentRound);
                }
            case Ack:
                // other caches need data from the update we pushed, so we need to stay in
                // shared stat, update main memory.
                currentRound = FireflyBusMessage.FinalWriteback(pendingRequest.getInAddress(), pendingRequest.getInData());
                return noJump(currentRound);
            case AckData:
                // got from other cache, go Shared
                line = controller.data.get(pendingRequest.getInAddress());
                line.data = b.data;
                switch (currentRound.type) {
                    case Get:
                        line.state = FireflyLineState.SHARED;
                        break;
                    default:
                        logger.fatal(("Apparently got ack in response to no-reply request type " + currentRound.type.toString()));
                        throw new RuntimeException();
                }
                Either<MemoryInstruction, FireflyBusMessage> result = handleClientRequest(pendingRequest, line);
                if (result.isFirst) {
                    return andJump(FireflyBusMessage.Done(), new FireflyDoneState(result.first, controller));
                } else {
                    currentRound = result.second;
                    return noJump(currentRound);
                }
            case AckDirty:
                // got line from other cache, store it in ours as above,
                // but arrange to writeback before we process
                line = controller.data.get(pendingRequest.getInAddress());
                line.data = b.data;
                switch (currentRound.type) {
                    case Get:
                        line.state = FireflyLineState.SHARED;
                        break;
                    default:
                        logger.fatal(("Apparently got ack in response to no-reply request type " + currentRound.type.toString()));
                        throw new RuntimeException();
                }
                currentRound = FireflyBusMessage.DirtyAckWriteback(pendingRequest.getInAddress(), b.data);
                return noJump(currentRound);
            default:
                logger.fatal("Unexpected bus response type " + b.type.toString());
                throw new RuntimeException();
        }
    }
}
