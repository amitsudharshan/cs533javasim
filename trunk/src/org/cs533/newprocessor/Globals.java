/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor;

/**
 *
 * @author amit
 */
public class Globals {

    public static final int TOTAL_MEMORY_SIZE = 4096; // 512KB memory
    public static final int CACHE_LINE_SIZE = 4; // 32bit line size
    public static final int L2_SIZE_IN_NUMBER_OF_LINES = 512;
    public static final int L1_SIZE_IN_NUMBER_OF_LINES = 64;
    /*
     * LATENCIES
     */
    public static final int MAIN_MEMORY_LATENCY = 10;
    public static final int L2_CACHE_LATENCY = 5;
    public static final int L1_CACHE_LATENCY = 1;

    /*
     * MISC L1 Parameters
     */
    public static final int MAX_NUMBER_BUS_MESSAGES_PER_CYCLE = 10;
}
