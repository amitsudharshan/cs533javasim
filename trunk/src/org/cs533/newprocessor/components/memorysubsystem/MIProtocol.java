/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;

import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author brandon
 */
public class MIProtocol
        implements CoherenceProtocol<MIProtocol.MIBusMessage, MIProtocol.MILineState> {

    ProtocolContext<MILineState> context;
    MemoryInstruction pendingRequest;
    CacheLine<MILineState> evictedLine;
    LRUEvictHashTable<CacheLine<MILineState>> data;

    /** If state is Ready, pendingRequest is non-null */
    private enum ProtocolState
    {
        RunningTransaction, FinishedRequest, GettingRequest, Ready, Uninitialized
    }
    ProtocolState state;

    void MIProtocol() {
        state = ProtocolState.Uninitialized;
    }

    public enum MILineState
    {
        MODIFIED, INVALID
    }

    public MIBusMessage GetX(int address) {
        return new MIBusMessage(MIBusMessageType.GETX, address, null);
    }
    public MIBusMessage Ack(int address, byte[] data){
        return new MIBusMessage(MIBusMessageType.DATA_ACK, address, data);
    }
    public MIBusMessage Nack(int address){
        return new MIBusMessage(MIBusMessageType.NACK, address, null);
    }
    public static class MIBusMessage
    {
        public final MIBusMessageType type;
        public final int address;
        public final byte[] data;

        public MIBusMessage(MIBusMessageType type, int address, byte[] data) {
            this.type = type;
            this.address = address;
            this.data = data;
        }
    }
    public static enum MIBusMessageType {
        GETX, DATA_ACK, NACK
    }

    public void setContext(ProtocolContext<MILineState> c) {
        context = c;
        data = context.getData();
        state = ProtocolState.GettingRequest;
    }

    public void recieveMessage(MIBusMessage msg) 
    {
        Simulator.logEvent("msg.received");
        int evictAddress = msg.address;
        CacheLine line = data.get(evictAddress);
        if (line != null)
        {
            
            line.state = MILineState.INVALID;
            //line.data =  ;
            data.lines.put(line.address, line);//invalidated line
        }      

    }

    public BusAggregator<MIBusMessage> getAggregator() {
        if (true)
            return null;
        int address;
        if (evictedLine != null) {
            address = evictedLine.address;
        } else {
            address = pendingRequest.getInAddress();
        }
        final int _address = address;
        return new BusAggregator<MIBusMessage>(){
            final int address = _address;
            byte[] data;
            void aggregate(MIBusMessage msg) {
                if (msg.type == MIBusMessageType.DATA_ACK) {
                    data = msg.data;
                }
            }
            MIBusMessage getResult() {
                if (data != null) {
                    return Ack(address,data);
                } else {
                    return Nack(address);
                }
            }
        };
    }

    public MIBusMessage getResponse() {
        return null;
    }

    public MIBusMessage getBusMessage()
    {
        if (state == ProtocolState.Ready) {
            state = ProtocolState.RunningTransaction;
            Simulator.logEvent("msg.invalidate");
            return GetX(pendingRequest.inAddress);
        } 
        else {
            return null;
        }
    }

    public MemoryInstruction getMemoryRequest()
    {
        if (pendingRequest != null) {
            return pendingRequest;
        } else {
            return null;
        }
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        if (state == ProtocolState.RunningTransaction) {
            state = ProtocolState.FinishedRequest;
            pendingRequest = null;
            
        }
    }

    public void runPrep() {
        if (state == ProtocolState.GettingRequest) {
            assert(pendingRequest == null);
            pendingRequest = context.getNextRequest();
        }
    }

    public void runClock()
    {
        if (state == ProtocolState.FinishedRequest) {
            state = ProtocolState.GettingRequest;
        } else if (state == ProtocolState.GettingRequest && pendingRequest != null) {
            CacheLine<MILineState> line = data.get(pendingRequest.getInAddress());
            if (line != null && line.state == MILineState.MODIFIED)
            {
                // handle request out of cache
                switch (pendingRequest.getType()) {
                    case Load:
                        Simulator.logEvent("Load");
                        pendingRequest.setOutData(line.data);
                        data.lines.put(line.address, line);
                        break;
                    case Store:
                        Simulator.logEvent("Store");
                        line.data = pendingRequest.getInData();
                        data.lines.put(line.address, line);
                        break;
                    case CAS:
                        if (line.data == pendingRequest.compareData)
                        {
                            pendingRequest.outData = line.data;
                            line.data = pendingRequest.inData;
                        }
                        //data.add(line);
                        break;
                    default:

                        break;
                }
                pendingRequest.setIsCompleted(true);
                // go on to the next request;
                pendingRequest = null;
            } else {
                // must go to bus
                state = ProtocolState.Ready;
            }
        }
    }

    public int getLatency() {
        return 1;
    }
}