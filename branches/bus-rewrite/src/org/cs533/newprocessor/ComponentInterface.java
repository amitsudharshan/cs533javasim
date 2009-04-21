/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor;

/**
 *
 * @author amit
 */
public interface ComponentInterface {

    /**
     * Read inputs for the next cycle from connected components
     */
    public abstract void runPrep();

   /**
    * Transition to the next state
    */
    public abstract void runClock();

    /**
     * Return the usual latency, in number of times runClock must
     * be called before the component makes progress.
     * @return expected component latency
     */
    public int getLatency();

}
