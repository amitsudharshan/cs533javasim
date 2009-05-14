/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheController;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public abstract class FireflyCacheControllerState extends CacheControllerState<FireflyBusMessage> {
    protected FireflyCacheController controller;

    public FireflyCacheControllerState(FireflyCacheController controller) {
        logger = controller.getChildLogger(this.getClass().getSimpleName());
        this.controller = controller;
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> snoopMemoryResponse(MemoryInstruction response) {
        logger.debug("snoopMemoryResponse");
        return noReply();
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> receiveBusResponse(FireflyBusMessage b) {
        logger.debug("receiveBusResponse");
        return noReply();
    }

    protected final FireflyBusMessage handleBroadcastMessage (FireflyBusMessage b) {
        logger.debug("handleBroadcastMessage");
        CacheLine<FireflyLineState> line;
        switch (b.type) {
            case Get:
                line = controller.data.get(b.address);
                if (line == null) {
                    return FireflyBusMessage.Nack();
                } else {
                    switch (line.state) {
                        case INVALID:
                            return FireflyBusMessage.Nack();
                        case EXCLUSIVE:
                            line.state = FireflyLineState.SHARED;
                        case SHARED:
                            return FireflyBusMessage.AckData(line.data);
                        case MODIFIED:
                            line.state = FireflyLineState.SHARED;
                            return FireflyBusMessage.AckDirty(line.data);
                        default:
                            throw new RuntimeException("Unknown line state handling Get "+line.state.toString());
                    }
                }
            case Update:
                line = controller.data.get(b.address);
                if (line == null) {
                    return FireflyBusMessage.Nack();
                } else {
                    switch (line.state) {
                        case INVALID:
                            return FireflyBusMessage.Nack();
                        case SHARED:
                            line.data = b.data;
                            return FireflyBusMessage.Ack();
                        case EXCLUSIVE:
                        case MODIFIED:
                            throw new RuntimeException("Got Update when had line in state "+line.state.toString());
                        default:
                            throw new RuntimeException("Unknown line state handling Update "+line.state.toString());
                    }
                }
            case Load:
                // someone else had to go to memory for the line, so we may only have it in INVALID state.
                line = controller.data.get(b.address);
                if (line != null && line.state != FireflyLineState.INVALID) {
                    logger.fatal("Got "+b.type.toString()+" in state " + line.state.toString());
                    throw new RuntimeException("Got "+b.type.toString()+" in state " + line.state.toString());
                } else {
                    return null;
                }
            case DirtyAckWriteback:
            case FinalWriteback:
            case Nack:
            case AckData:
            case AckDirty:
            case Done:
                return null;
            default:
                logger.fatal("Unknown bus message type " + b.type.toString());
                throw new RuntimeException();
        }
    }

/** 
 * Returns either a MemoryInstruction describing the completed result for this request,
 * or a BusMessage describing the nextr transaction phase we need to run.
 * @pre assumes space has been allocated for request.getInAddress() in the cache. */
    protected final Either<MemoryInstruction, FireflyBusMessage> handleClientRequest(MemoryInstruction request, CacheLine<FireflyLineState> line) {
        if (line == null || line.state == FireflyLineState.INVALID) {
            switch (request.getType()) {
                case Load:
                case Store:
                case CAS:
                    return Either.Right(FireflyBusMessage.Get(request.getInAddress()));
                default:
                    logger.fatal("Unknown memory request type " + request.getClass().toString());
                    throw new RuntimeException();
            }
        } else {
            switch (request.getType()) {
                case Load:
                    request.setOutData(line.data);
                    return Either.Left(request);
                case Store:
                    switch (line.state) {
                        case SHARED:
                            return Either.Right(FireflyBusMessage.Update(request.getInAddress(),request.getInData()));
                        case EXCLUSIVE:
                            line.state = FireflyLineState.MODIFIED;
                        case MODIFIED:
                            line.data = request.getInData();
                            return Either.Left(request);
                    }
                case CAS:
                    request.setOutData(line.data);
                    if (!Arrays.equals(line.data, request.getCompareData())) {
                        return Either.Left(request);
                    }
                    switch (line.state) {
                        case SHARED:
                            // will have to remember someone might update the line so the CAS fails before we get the bus.
                            return Either.Right(FireflyBusMessage.Update(request.getInAddress(),request.getInData()));
                        case EXCLUSIVE:
                            line.state = FireflyLineState.MODIFIED;
                        case MODIFIED:
                            line.data = request.getInData();
                            return Either.Left(request);
                    }
                default:
                    logger.fatal("Unknown memory request type " + request.getClass().toString());
                    throw new RuntimeException();
            }
        }
    }

    protected final StateAnd<MemoryInstruction,CacheControllerState<FireflyBusMessage>> handleClientRequestAsMemory(MemoryInstruction request, CacheLine<FireflyLineState> line) {
        Either<MemoryInstruction,FireflyBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            return noJump(result.first);
        } else {
            return jumpTo(new FireflyReadyState(request, controller));
        }
    }
}