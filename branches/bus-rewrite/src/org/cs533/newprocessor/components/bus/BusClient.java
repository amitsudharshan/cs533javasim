/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

/**
 * A client on a cache coherence bus. During the
 * {@link org.cs533.newprocessor.components.ComponentInterface#runPrep runPrep}
 * phase the bus may look for a compenent ready to initiate a transaction by
 * calling getResponse and looking for a non-null message. If so, getAggregator
 * will also be called to see if the client wants a response, and how to
 * aggregate it. recieveMessage will be invoked on other clients to pass
 * the message along. If an aggregator was set then in the next cycle
 * getResponse will be called on these clients, and the responses aggregated
 * with the returned aggregator.
 * @author brandon
 */
public interface BusClient {
    void recieveMessage(BusMessage msg);
    BusMessage getResponse();
    BusMessage getBusMessage();
    BusMessageAggregator getAggregator();
};
