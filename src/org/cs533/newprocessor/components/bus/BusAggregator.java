/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

/**
 *
 * @author brandon
 */
public interface BusAggregator<BusMessage> {
    public void aggregate(BusMessage msg);
    public BusMessage getResult();
}
