package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class FireflyBusMessage extends AbstractBusMessage<FireflyBusMessage> {

    public static enum FireflyBusMessageType {

        Get, Update, Ack, AckData, AckDirty, Nack, DirtyAckWriteback, FinalWriteback, Load, Done
    }
    public final FireflyBusMessage.FireflyBusMessageType type;
    public final int address;
    public final byte[] data;

    private static class AckDataAggregator implements BusAggregator<FireflyBusMessage> {
        FireflyBusMessage ack;
        public void aggregate(FireflyBusMessage msg) {
            switch(msg.type) {
                case AckDirty:
                    assert ack == null;
                    ack = msg;
                    break;
                case AckData:
                    assert ack == null || ack.type == FireflyBusMessageType.AckData;
                    ack = msg;
                    break;
                case Nack:
                    break;
                default:
                    throw new RuntimeException("unexpected reply type "+msg.type.toString());
            }
        }
        public FireflyBusMessage getResult() {
            if (ack != null) {
                return ack;
            } else {
                return FireflyBusMessage.Nack();
            }
        }
    }
    private static class AckBooleanAggregator implements BusAggregator<FireflyBusMessage> {
        FireflyBusMessage ack;
        public void aggregate(FireflyBusMessage msg) {
            switch (msg.type) {
                case Ack:
                    ack = msg;
                    break;
                case Nack:
                    break;
                default:
                    throw new RuntimeException("unexpected reply type "+msg.type.toString());
            }
        }
        public FireflyBusMessage getResult() {
            if (ack != null) {
                return ack;
            } else {
                return FireflyBusMessage.Nack();
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
            case Ack:
            case AckData:
            case AckDirty:
            case Nack:
            case Done:
                return getTypeString();
            case Get:
            case Update:
            case Load:
            case DirtyAckWriteback:
            case FinalWriteback:
                return getTypeString()+"("+Integer.toString(address)+")";
            default:
                return "Unknown FireflyBusMessage Type "+type.toString();
        }
    }
    
    private FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType type, int address, byte[] data,
            BusAggregator<FireflyBusMessage> aggregator, MemoryInstruction memoryRequest) {
        this.type = type;
        this.address = address;
        this.data = data;
        this.aggregator = aggregator;
        this.memoryRequest = memoryRequest;
    }

    public static FireflyBusMessage Get(int address) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Get, address, null, new AckDataAggregator(), null);
    }

    public static FireflyBusMessage Update(int address, byte[] data) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Update, address, data, new AckBooleanAggregator(), null);
    }

    public static FireflyBusMessage Ack() {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Ack, -1, null, null, null);
    }
    public static FireflyBusMessage AckData(byte[] data) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.AckData, -1, data, null, null);
    }
    public static FireflyBusMessage AckDirty(byte[] data) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.AckDirty, -1, data, null, null);
    }

    public static FireflyBusMessage Nack() {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Nack, -1, null, null, null);
    }
    public static FireflyBusMessage Done() {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Done, -1, null, null, null);
    }

    public static FireflyBusMessage DirtyAckWriteback(int address, byte[] data) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.DirtyAckWriteback, address, null, null, MemoryInstruction.Store(address, data));
    }
    public static FireflyBusMessage FinalWriteback(int address, byte[] data) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.FinalWriteback, address, null, null, MemoryInstruction.Store(address, data));
    }

    public static FireflyBusMessage Load(int address) {
        return new FireflyBusMessage(FireflyBusMessage.FireflyBusMessageType.Load, address, null, null, MemoryInstruction.Load(address));
    }
}