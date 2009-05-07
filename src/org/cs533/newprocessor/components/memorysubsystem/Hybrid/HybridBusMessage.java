package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class HybridBusMessage extends AbstractBusMessage<HybridBusMessage> {

    public static enum HybridBusMessageType {

        Get, GetX, Invalidate, Update, AckData, AckDirty, Nack, Writeback, Load, Done
    }
    public final HybridBusMessage.HybridBusMessageType type;
    public final int address;
    public final byte[] data;

    private static class AckAggregator implements BusAggregator<HybridBusMessage> {
        HybridBusMessage ack;
        public void aggregate(HybridBusMessage msg) {
            if (msg.type == HybridBusMessageType.AckDirty) {
                assert ack == null;
                ack = msg;
            }
            if (ack == null) {
                if (msg.type == HybridBusMessageType.AckData) {
                    ack = msg;
                }
            }
        }
        public HybridBusMessage getResult() {
            if (ack != null) {
                return ack;
            } else {
                return HybridBusMessage.Nack();
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
            case Update:
            case Invalidate:
            case Load:
            case Writeback:
                return getTypeString()+"("+Integer.toString(address)+")";
            default:
                return "Unknown HybridBusMessage Type "+type.toString();
        }
    }
    
    private HybridBusMessage(HybridBusMessage.HybridBusMessageType type, int address, byte[] data,
            BusAggregator<HybridBusMessage> aggregator, MemoryInstruction memoryRequest) {
        this.type = type;
        this.address = address;
        this.data = data;
        this.aggregator = aggregator;
        this.memoryRequest = memoryRequest;
    }

    public static HybridBusMessage Get(int address) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Get, address, null, new AckAggregator(), null);
    }

    public static HybridBusMessage GetX(int address) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.GetX, address, null, new AckAggregator(), null);
    }

    public static HybridBusMessage Invalidate(int address) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Invalidate, address, null, null, null);
    }

    public static HybridBusMessage Update(int address, byte[] data) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Update, address, data, null, null);
    }
    
    public static HybridBusMessage AckData(byte[] data) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.AckData, -1, data, null, null);
    }

    public static HybridBusMessage AckDirty(byte[] data) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.AckDirty, -1, data, null, null);
    }

    public static HybridBusMessage Nack() {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Nack, -1, null, null, null);
    }
    public static HybridBusMessage Done() {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Done, -1, null, null, null);
    }

    public static HybridBusMessage Writeback(int address, byte[] data) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Writeback, address, null, null, MemoryInstruction.Store(address, data));
    }

    public static HybridBusMessage Load(int address) {
        return new HybridBusMessage(HybridBusMessage.HybridBusMessageType.Load, address, null, null, MemoryInstruction.Load(address));
    }
}