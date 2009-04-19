/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.processor;

/**
 *
 * @author Vivek
 */
public class MainMemory extends AbstractComponent {

    public static final int BANDWIDTH = 64;
    public static final int LATENCY = 100;

    public MainMemory() {
        super(BANDWIDTH, LATENCY);
    }
}
