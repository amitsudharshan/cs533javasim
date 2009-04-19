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

    public abstract void runPrep();

    public abstract void runClock();

    public int getLatency();

}
