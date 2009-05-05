/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class AbstractBusMessage<M extends AbstractBusMessage<M>> {
    protected BusAggregator<M> aggregator;
    protected MemoryInstruction memoryRequest;

    public BusAggregator<M> getAggregator() {
        return aggregator;
    }
    public MemoryInstruction getMemoryRequest() {
        return memoryRequest;
    }
    public String getTypeString() {
        return null;
    }
}