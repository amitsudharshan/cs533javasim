/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols;

import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1CacheLine;

/**
 *
 * @author amit
 */
public abstract class AbstractProtocol {

    public States states;

    public enum Operations {

        Read, Write
    }

    public class BusMessage {

        Operations opType;
        byte[] data;
        int address;
        int response;
        public BusMessage(Operations opType, int address, byte[] data) {
            this.opType = opType;
            this.data = data;
            this.address = address;
        }

        public void setResponse(int response) {
            this.response = response;
        }

        public int getResponse() {
            return response;
        }
        public int getAddress() {
            return address;
        }

        public byte[] getData() {
            return data;
        }

        public Operations getOpType() {
            return opType;
        }
    }

    public enum CACHE_STATUS {

        WRITE_HIT, WRITE_MISS, READ_HIT, READ_MISS
    }

    public enum BUS_MESSAGES {

        BUS_WRITE_MISS, BUS_INVALIDATE, BUS_READ
    }

    public AbstractProtocol(String[] states_) {
        states = new States(states_);
    }

    public abstract BusMessage getNextState(L1CacheLine line, CACHE_STATUS status);

    public abstract void handleMessage(BusMessage message, L1CacheLine line);

}
