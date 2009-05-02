/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;
import org.cs533.newprocessor.simulator.Simulator;
import java.util.Arrays;

/**
 *
 * @author brandon
 */
public class MIProtocol
        implements CoherenceProtocol<MIProtocol.MIBusMessage, MIProtocol.MILineState> {

    ProtocolContext<MILineState> context;
    MemoryInstruction pendingRequest;
    CacheLine<MILineState> evictedLine;
    MIBusMessage response;
    LRUEvictHashTable<CacheLine<MILineState>> data;

    /** Invariants
     * when Ready, pendingRequest is non-null
     *
     * when running a transaction with evictedLine null, we
     *  have an INVALID line allocated for the address of the request
     *  in our cache
     */
    private enum ProtocolState {

        QueryingSiblings, HittingMemory, FinishedRequest, GettingRequest, Ready, Uninitialized
    }
    ProtocolState state;

    void MIProtocol() {
        state = ProtocolState.Uninitialized;
    }

    public enum MILineState {

        MODIFIED, INVALID
    }

    public static enum MIBusMessageType {

        GETX, DATA_ACK, NACK,
        WRITEBACK
    }

    public static class MIBusMessage {

        public final MIBusMessageType type;
        public final int address;
        public final byte[] data;

        public MIBusMessage(MIBusMessageType type, int address, byte[] data) {
            this.type = type;
            this.address = address;
            this.data = data;
        }
    }

    private MIBusMessage GetX(int address) {
        return new MIBusMessage(MIBusMessageType.GETX, address, null);
    }

    private MIBusMessage Ack(byte[] data) {
        return new MIBusMessage(MIBusMessageType.DATA_ACK, -1, data);
    }

    private MIBusMessage Nack() {
        return new MIBusMessage(MIBusMessageType.NACK, -1, null);
    }

    private MIBusMessage Writeback(int address) {
        return new MIBusMessage(MIBusMessageType.WRITEBACK, address, null);
    }

    public void setContext(ProtocolContext<MILineState> c) {
        context = c;
        data = context.getData();
        state = ProtocolState.GettingRequest;
    }

    // responding to another transaction
    // or responses in our own
    public void recieveMessage(MIBusMessage msg) {
        if (state != ProtocolState.QueryingSiblings) {
            Simulator.logEvent("msg.received-type:" + msg.type.toString());
            switch (msg.type) {
                case GETX:
                    CacheLine<MILineState> line = data.get(msg.address);
                    if (line != null && line.state == MILineState.MODIFIED) {
                        line.state = MILineState.INVALID;
                        response = Ack(line.data);
                    } else {
                        response = Nack();
                    }
                    break;
                case DATA_ACK:
                case NACK:
                case WRITEBACK:
                    // ignore
                    break;
            }
        } else {
            // only solicit responses on memory requests,
            // where we might need to get the line by cache-to-cache transfer
            assert (evictedLine == null);
            switch (msg.type) {
                case DATA_ACK:
                    // got data
                    CacheLine<MILineState> line = data.get(pendingRequest.getInAddress());
                    line.data = msg.data;
                    handle_request_locally(line);
                    state = ProtocolState.FinishedRequest;
                    break;
                case NACK:
                    // no data, have to go to memory
                    state = ProtocolState.HittingMemory;
                    break;
                // GETX, WRITEBACK should be impossible
            }
        }
    }

    public MIBusMessage getResponse() {
        if (response != null) {
            MIBusMessage theResponse = response;
            response = null;
            return theResponse;
        }
        return null;
    }

    // leading a transaction
    public MIBusMessage getBusMessage() {
        if (state == ProtocolState.Ready) {
            if (evictedLine != null) {
                state = ProtocolState.HittingMemory;
                Simulator.logEvent("msg.invalidate-writeback");
                return Writeback(evictedLine.address);
            } else {
                state = ProtocolState.QueryingSiblings;
                Simulator.logEvent("msg.invalidate");
                return GetX(pendingRequest.inAddress);
            }
        } else {
            return null;
        }
    }

    public BusAggregator<MIBusMessage> getAggregator() {
        if (state != ProtocolState.QueryingSiblings) {
            return null;
        }
        return new BusAggregator<MIBusMessage>() {

            byte[] data;

            public void aggregate(MIBusMessage msg) {
                if (msg.type == MIBusMessageType.DATA_ACK) {
                    data = msg.data;
                }
            }

            public MIBusMessage getResult() {
                if (data != null) {
                    return Ack(data);
                } else {
                    return Nack();
                }
            }
        };
    }

    public MemoryInstruction getMemoryRequest() {
        if (state != ProtocolState.HittingMemory) {
            return null;
        }
        if (evictedLine != null) {
            return MemoryInstruction.Store(evictedLine.address, evictedLine.data);
        } else if (pendingRequest != null) {
            return MemoryInstruction.Load(pendingRequest.getInAddress());
        }
        assert false;
        return null;
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        if (state == ProtocolState.HittingMemory) {
            if (evictedLine != null) {
                // upload finished
                evictedLine = null;
            } else {
                assert (pendingRequest != null);
                CacheLine<MILineState> line = data.get(pendingRequest.getInAddress());
                line.state = MILineState.MODIFIED;
                line.data = resp.getOutData();
                handle_request_locally(line);
                state = ProtocolState.FinishedRequest;
            }
        }
    }

    public void runPrep() {
        if (state == ProtocolState.GettingRequest) {
            assert (pendingRequest == null);
            pendingRequest = context.getNextRequest();
        }
    }

    public void runClock() {
        if (state == ProtocolState.FinishedRequest) {
            state = ProtocolState.GettingRequest;
        } else if (state == ProtocolState.GettingRequest && pendingRequest != null) {
            CacheLine<MILineState> line = data.get(pendingRequest.getInAddress());
            if (line != null && line.state == MILineState.MODIFIED) {
                Simulator.logEvent("L1-CacheHit");
                // handle request out of cache
                handle_request_locally(line);
                assert (pendingRequest == null);
            } else if (line != null && line.state == MILineState.INVALID) {
                // must go to bus
                state = ProtocolState.Ready;
                Simulator.logEvent("L1-CacheMiss");
            } else {
                CacheLine<MILineState> newLine = new CacheLine<MILineState>(pendingRequest.getInAddress(), null, MILineState.INVALID);
                evictedLine = data.add(newLine);
                if (evictedLine != null && evictedLine.state == MILineState.MODIFIED) {
                    Simulator.logEvent("L1-CacheMiss-w/eviction");
                } else {
                    assert (evictedLine.state == MILineState.INVALID);
                    evictedLine = null;
                    Simulator.logEvent("L1-CacheMiss");
                }
                state = ProtocolState.Ready;
            }
        }
    }

    private void handle_request_locally(CacheLine<MILineState> line) {
        switch (pendingRequest.getType()) {
            case Load:
                Simulator.logEvent("Load");
                pendingRequest.setOutData(line.data);
                pendingRequest.setIsCompleted(true);
                break;
            case Store:
                Simulator.logEvent("Store");
                line.data = pendingRequest.getInData();
                pendingRequest.setIsCompleted(true);
                break;
            case CAS:
                if (Arrays.equals(line.data, pendingRequest.compareData)) {
                    pendingRequest.outData = line.data;
                    line.data = pendingRequest.inData;
                }
                pendingRequest.setIsCompleted(true);
                break;
        }
        pendingRequest = null;
    }

    public int getLatency() {
        return 1;
    }
}