/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.processor;

/**
 *
 * @author Vivek
 */
public class Cache extends AbstractComponent{
    public static final int BANDWIDTH = 64;
    public static final int LATENCY = 1;
    public Cache() {
        super(BANDWIDTH,LATENCY);
    }

}
