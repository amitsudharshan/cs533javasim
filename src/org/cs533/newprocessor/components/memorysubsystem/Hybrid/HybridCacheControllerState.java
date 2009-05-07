/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.Either;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction.SubTypes;

/**
 *
 * @author amit
 */
public abstract class HybridCacheControllerState extends CacheControllerState<HybridBusMessage> {
    protected HybridCacheController controller;
    // track an address we dirtyAcked, so we're not surprised to see it written back
    protected int dirtyWritebackAddr = -1;

    public HybridCacheControllerState(HybridCacheController controller) {
        logger = Logger.getLogger(controller.toString()+"."+this.getClass().getSimpleName());
        this.controller = controller;
    }

    @Override
    public StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> snoopMemoryResponse(MemoryInstruction response) {
        logger.debug("snoopMemoryResponse");
        return noReply();
    }

    protected final StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> handleBroadcastMessage (HybridBusMessage b) {
        logger.debug("handleBroadcastMessage");
        CacheLine<HybridLineState> line;
        switch (b.type) {
            case Get:
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(HybridBusMessage.Nack());
                } else {
                    switch (line.state) {
                        case INVALID:
                            return noJump(HybridBusMessage.Nack());
                        case EXCLUSIVE:
                            line.state = HybridLineState.SHARED;
                        case SHARED:
                            return noJump(HybridBusMessage.AckData(line.data));
                        case MODIFIED:
                            line.state = HybridLineState.SHARED;
                            dirtyWritebackAddr = line.address;
                            return noJump(HybridBusMessage.AckDirty(line.data));
                        default:
                            throw new RuntimeException("Unknown line state handling Get "+line.state.toString());
                    }
                }
            case GetX:
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(HybridBusMessage.Nack());
                } else {
                    switch (line.state) {
                        case INVALID:
                            return noJump(HybridBusMessage.Nack());
                        case EXCLUSIVE:
                        case SHARED:
                            line.state = HybridLineState.INVALID;
                            return noJump(HybridBusMessage.AckData(line.data));
                        case MODIFIED:
                            line.state = HybridLineState.INVALID;
                            return noJump(HybridBusMessage.AckDirty(line.data));
                        default:
                            throw new RuntimeException("Unknown line state handling GetX "+line.state.toString());
                    }
                }
            case Update:
                line = controller.data.get(b.address);
                if (line == null) {
                    return noJump(HybridBusMessage.Nack());
                } else { 
                    switch (line.state) {
                        case SHARED:
                            line.data = b.data;
                            return noJump(HybridBusMessage.AckData(null));
                        case INVALID:
                            return noJump(HybridBusMessage.Nack());
                        default:
                            logger.fatal("Got UPDATE in state " + line.state.toString());
                            throw new RuntimeException("Got UPDATE in state " + line.state.toString());
                    }
                }
            case Invalidate:
                // should only occur when upgrading a SHARED line to exclusive
                line = controller.data.get(b.address);
                if (line == null) {
                    return noReply();
                } else {
                    switch (line.state) {
                        case SHARED:
                            line.state = HybridLineState.INVALID;
                        case INVALID:
                            return noReply();
                        default:
                            logger.fatal("Got INVALIDATE in state " + line.state.toString());
                            throw new RuntimeException("Got INVALIDATE in state " + line.state.toString());
                    }
                }
            case Load:
            case Writeback:
                // someone else had to go to memory for the line, so we may only have it in INVALID state.
                line = controller.data.get(b.address);
                if (dirtyWritebackAddr == b.address) {
                    // unless they were doing a writeback for us
                    dirtyWritebackAddr = -1;
                } else {
                    if (line != null && line.state != HybridLineState.INVALID) {
                        logger.fatal("Got "+b.type.toString()+" in state " + line.state.toString());
                        throw new RuntimeException("Got "+b.type.toString()+" in state " + line.state.toString());
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
    protected final Either<MemoryInstruction, HybridBusMessage> handleClientRequest(MemoryInstruction request, CacheLine<HybridLineState> line) {
        if (line == null || line.state == HybridLineState.INVALID) {
            switch (request.getType()) {
                case Load:
                    return Either.Right(HybridBusMessage.Get(request.getInAddress()));
                case Store:
                    if (request.getSubType() == SubTypes.UpdateStore) {
                        return Either.Right(HybridBusMessage.Get(request.getInAddress()));
                    } else {
                        return Either.Right(HybridBusMessage.GetX(request.getInAddress()));
                    }
                case CAS:
                    return Either.Right(HybridBusMessage.GetX(request.getInAddress()));
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
                            if (request.getSubType() == SubTypes.UpdateStore) {
                                return Either.Right(HybridBusMessage.Update(request.getInAddress(), request.getInData()));
                            } else {
                                return Either.Right(HybridBusMessage.Invalidate(request.getInAddress()));
                            }
                        case EXCLUSIVE:
                            line.state = HybridLineState.MODIFIED;
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
                            return Either.Right(HybridBusMessage.Invalidate(request.getInAddress()));
                        case EXCLUSIVE:
                            line.state = HybridLineState.MODIFIED;
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

    protected final StateAnd<MemoryInstruction,CacheControllerState<HybridBusMessage>> handleClientRequestAsMemory(MemoryInstruction request, CacheLine<HybridLineState> line) {
        Either<MemoryInstruction,HybridBusMessage> result = handleClientRequest(request, line);
        if (result.isFirst) {
            return noJump(result.first);
        } else {
            return jumpTo(new HybridReadyState(result.second, request, controller));
        }
    }
}