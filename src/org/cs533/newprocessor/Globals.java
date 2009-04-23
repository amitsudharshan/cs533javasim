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

    public static final int WORD_SIZE =4; // 4 byte words
    public static final int TOTAL_MEMORY_SIZE = 512*1024; // 512KB memory
    public static final int CACHE_LINE_SIZE = 4; // 32bit line size
    public static final int L2_SIZE_IN_NUMBER_OF_LINES = 32768/CACHE_LINE_SIZE; // roughly 6% of TOTAL_MEMORY_SIZE
    public static final int L1_SIZE_IN_NUMBER_OF_LINES = 8192/CACHE_LINE_SIZE; // roughly 1% of TOTAL_MEMORY_SIZE
    /*
     * LATENCIES
     */
    public static final int MAIN_MEMORY_LATENCY = 50;
    public static final int L2_CACHE_LATENCY = 5;
    public static final int L1_CACHE_LATENCY = 1;
    public static final int CACHE_COHERENCE_BUS_LATENCY = 15;
    /*
     * MISC L1 Parameters
     */

    public static final int MAX_NUMBER_L1_BUS_MESSAGES_PER_CYCLE = 10;

    public static final int OPCODE_LENGTH = 3;
    public static final int INSTRUCTION_LENGTH = 32;

    public static final int MEMORY_ADDRESS_LENGTH = 19;
  


}

