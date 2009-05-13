/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public abstract class MESICacheControllerState extends CacheControllerState<MESIBusMessage> {
    protected MESICacheController controller;

    public MESICacheControllerState(MESICacheController controller) {
        logger = controller.getChildLogger(this.getClass().getSimpleName());
        this.controller = controller;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> snoopMemoryResponse(MemoryInstruction response) {
        return noReply();
    }

    protected final StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> handleBroadcastMessage (MESIBusMessage b) {
        CacheLine<MESILineState> line;
        switch (b.type) {
            case Get:
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(MESIBusMessage.Nack());
                } else {
                    switch (line.state) {
                        case INVALID:
                            return noJump(MESIBusMessage.Nack());
                        case EXCLUSIVE:
                            line.state = MESILineState.SHARED;
                        case SHARED:
                            return noJump(MESIBusMessage.AckData(line.data));
                        case MODIFIED:
                            line.state = MESILineState.SHARED;
                            controller.dirtyWritebackAddr = line.address;
                            return noJump(MESIBusMessage.AckDirty(line.data));
                        default:
                            throw new RuntimeException("Unknown line state handling Get "+line.state.toString());
                    }
                }
            case GetX:
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(MESIBusMessage.Nack());
                } else {
                    switch (line.state) {
                        case INVALID:
                            return noJump(MESIBusMessage.Nack());
                        case EXCLUSIVE:
                        case SHARED:
                            line.state = MESILineState.INVALID;
                            return noJump(MESIBusMessage.AckData(line.data));
                        case MODIFIED:
                            line.state = MESILineState.INVALID;
                            return noJump(MESIBusMessage.AckDirty(line.data));
                        default:
                            throw new RuntimeException("Unknown line state handling GetX "+line.state.toString());
                    }
                }
            case Invalidate:
                // should only occur when upgrading a SHARED line to exclusive
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(MESIBusMessage.Nack());
                } else {
                    switch (line.state) {
                        case SHARED:
                            line.state = MESILineState.INVALID;
                        case INVALID:
                            return noJump(MESIBusMessage.Nack());
                        default:
                            logger.fatal("Got INVALIDATE in state " + line.state.toString());
                            throw new RuntimeException("Got INVALIDATE in state " + line.state.toString());
                    }
                }
            case Load:
            case Writeback:
                // someone else had to go to memory for the line, so we may only have it in INVALID state.
                line = controller.data.get(b.address);
                if (controller.dirtyWritebackAddr == b.address) {
                    // unless they were doing a writeback for us
                    if (line != null && !(line.state == MESILineState.INVALID || line.state == MESILineState.SHARED)) {
                        logger.fatal("Saw own dirty Writeback with line in unexpected state " + line.state.toString()+", cache state "+this.toString());
                        throw new RuntimeException("Saw own dirty Writeback with line in unexpected state " + line.state.toString()+", cache state "+this.toString());
                    }
                    controller.dirtyWritebackAddr = -1;
                } else {
                    if (line != null && line.state != MESILineState.INVALID) {
                        logger.fatal("Got Writeback with line in state " + line.state.toString()+", cache state "+this.toString());
                        throw new RuntimeException("Got Writeback with line in state " + line.state.toString()+", cache state "+this.toString());
                    } else {
                        return noReply();
                    }
                }
            case Nack:
            case AckData:
            case AckDirty:
            case Done:
                return noReply();
            default:
                logger.fatal("Unknown bus message type " + b.type.toString());
                throw new RuntimeException();
        }
    }

/** 
 * Returns either a MemoryInstruction describing the completed result for this request,
 * or a BusMessage describing the nextr transaction phase we need to run.
 * @pre assumes space has been allocated for request.getInAddress() in the cache. */
    protected final Either<MemoryInstruction, MESIBusMessage> handleClientRequest(MemoryInstruction request, CacheLine<MESILineState> line) {
        if (line == null || line.state == MESILineState.INVALID) {
            switch (request.getType()) {
                case Load:
                    return Either.Right(MESIBusMessage.Get(request.getInAddress()));
                case Store:
                case CAS:
                    return Either.Right(MESIBusMessage.GetX(request.getInAddress()));
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
                            return Either.Right(MESIBusMessage.Invalidate(request.getInAddress()));
                        case EXCLUSIVE:
                            line.state = MESILineState.MODIFIED;
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
                            return Either.Right(MESIBusMessage.Invalidate(request.getInAddress()));
                        case EXCLUSIVE:
                            line.state = MESILineState.MODIFIED;
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
    protected final StateAnd<MESIBusMessage,CacheControllerState<MESIBusMessage>> handleClientRequestAsMessage(MemoryInstruction request, CacheLine<MESILineState> line) {
        Either<MemoryInstruction,MESIBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            // nack acts as transaction done, because it has null aggregator and request
            return andJump(MESIBusMessage.Done(), new MESIDoneState(result.first, controller));
        } else {
            return noJump(result.second);
        }
    }
    protected final StateAnd<MemoryInstruction,CacheControllerState<MESIBusMessage>> handleClientRequestAsMemory(MemoryInstruction request, CacheLine<MESILineState> line) {
        Either<MemoryInstruction,MESIBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            return noJump(result.first);
        } else {
            return jumpTo(new MESIReadyState(result.second, request, controller));
        }
    }
}