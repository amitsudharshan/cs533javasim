/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor;

/**
 *
 * @author amit
 */
public abstract class AbstractComponent {

    int latency;
    int bandwidth;

    public AbstractComponent(int bandwidth, int latency) {
        this.latency = latency;
        this.bandwidth = bandwidth;
    }

    public abstract void runPrep();

    public abstract void runClock();
}
