/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1Cache;

/**
 *
 * @author brandon
 */
public class BusMessage implements Comparable {

    public BusMessage messageFromSender = null;
    public CacheCoherenceBus.MessageTypes messageType = null;
    public CacheCoherenceBus.ResponseTypes response = null;
    public L1Cache l1Cache = null; // the L1Cache originating the message.
    public int address = -1; // the address we are searching for (NORMALIZED...)
    public byte[] inData = null; // Data we may be writing
    public byte[] outData = null; // Data we may need to get out of this message
    private int waitCycles = 0;

    public BusMessage(BusMessage messageFromSender, CacheCoherenceBus.MessageTypes messageType, CacheCoherenceBus.ResponseTypes response, L1Cache l1Cache, int address, byte[] inData, byte[] outData) {
        this.messageFromSender = messageFromSender;
        this.messageType = messageType;
        this.response = response;
        this.l1Cache = l1Cache;
        this.address = address;
        this.inData = inData;
        this.outData = outData;
    }

    public BusMessage(BusMessage origMessage, L1Cache newCache) {
        messageFromSender = origMessage;
        address = origMessage.address;
        l1Cache = newCache;
    }

    public int compareTo(Object o) {
        return new Integer(waitCycles).compareTo((Integer) o);
    }
}
