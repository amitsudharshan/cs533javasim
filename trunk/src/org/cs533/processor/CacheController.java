/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.processor;

/**
 *
 * @author Vivek
 */
public class CacheController extends AbstractComponent {

    public static final int BANDWIDTH = -1; // I DON"T KNOW A GOOD VALUE HERE
    public static final int LATENCY = 1;

    public CacheController() {
        super(BANDWIDTH, LATENCY);
    }
}
