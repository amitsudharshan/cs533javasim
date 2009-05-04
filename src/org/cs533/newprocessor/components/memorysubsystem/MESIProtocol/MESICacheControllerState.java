/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import java.util.Arrays;
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
        this.controller = controller;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> snoopMemoryResponse(MemoryInstruction response) {
        return ignore();
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
                            return noJump(MESIBusMessage.AckDirty(line.data));
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
                            controller.logger.fatal("Got INVALIDATE in state " + line.state.toString());
                            throw new RuntimeException();
                    }
                }
            case Writeback:
                // someone else had the line modified, so we can only have it INVALID.
                line = controller.data.get(b.address);
                if (line != null && line.state != MESILineState.INVALID) {
                    controller.logger.fatal("Got WRITEBACK in state " + line.state.toString());
                    throw new RuntimeException();
                } else {
                    return ignore();
                }
            case Nack:
            case AckData:
            case AckDirty:
                return ignore();
            default:
                controller.logger.fatal("Unknown bus message type " + b.type.toString());
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
                    controller.logger.fatal("Unknown memory request type " + request.getClass().toString());
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
                    controller.logger.fatal("Unknown memory request type " + request.getClass().toString());
                    throw new RuntimeException();
            }
        }
    }
    protected final StateAnd<MESIBusMessage,CacheControllerState<MESIBusMessage>> handleClientRequestAsMessage(MemoryInstruction request, CacheLine<MESILineState> line) {
        Either<MemoryInstruction,MESIBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            return jumpTo(new MESIDoneState(result.first, controller));
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