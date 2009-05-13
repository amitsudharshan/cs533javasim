package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class MESIBusMessage extends AbstractBusMessage<MESIBusMessage> {

    public static enum MESIBusMessageType {

        Get, GetX, Invalidate, AckData, AckDirty, Nack, Writeback, Load, Done
    }
    public final MESIBusMessage.MESIBusMessageType type;
    public final int address;
    public final byte[] data;

    private static class AckAggregator implements BusAggregator<MESIBusMessage> {
        MESIBusMessage ack;
        public void aggregate(MESIBusMessage msg) {
            if (msg.type == MESIBusMessageType.AckDirty) {
                assert ack == null;
                ack = msg;
            }
            if (ack == null) {
                if (msg.type == MESIBusMessageType.AckData) {
                    ack = msg;
                }
            }
        }
        public MESIBusMessage getResult() {
            if (ack != null) {
                return ack;
            } else {
                return MESIBusMessage.Nack();
            }
        }
    }

    @Override
    public String getTypeString() {
        return type.toString();
    }
    @Override
    public String toString() {
        switch (type) {
            case AckData:
            case AckDirty:
            case Nack:
            case Done:
                return getTypeString();
            case Get:
            case GetX:
            case Invalidate:
            case Load:
            case Writeback:
                return getTypeString()+"("+Integer.toString(address)+")";
            default:
                return "Unknown MESIBusMessage Type "+type.toString();
        }
    }
    
    private MESIBusMessage(MESIBusMessage.MESIBusMessageType type, int address, byte[] data,
            BusAggregator<MESIBusMessage> aggregator, MemoryInstruction memoryRequest) {
        this.type = type;
        this.address = address;
        this.data = data;
        this.aggregator = aggregator;
        this.memoryRequest = memoryRequest;
    }

    public static MESIBusMessage Get(int address) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Get, address, null, new AckAggregator(), null);
    }

    public static MESIBusMessage GetX(int address) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.GetX, address, null, new AckAggregator(), null);
    }

    public static MESIBusMessage Invalidate(int address) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Invalidate, address, null, new AckAggregator(), null);
    }

    public static MESIBusMessage AckData(byte[] data) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.AckData, -1, data, null, null);
    }

    public static MESIBusMessage AckDirty(byte[] data) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.AckDirty, -1, data, null, null);
    }

    public static MESIBusMessage Nack() {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Nack, -1, null, null, null);
    }
    public static MESIBusMessage Done() {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Done, -1, null, null, null);
    }

    public static MESIBusMessage Writeback(int address, byte[] data) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Writeback, address, null, null, MemoryInstruction.Store(address, data));
    }

    public static MESIBusMessage Load(int address) {
        return new MESIBusMessage(MESIBusMessage.MESIBusMessageType.Load, address, null, null, MemoryInstruction.Load(address));
    }
}