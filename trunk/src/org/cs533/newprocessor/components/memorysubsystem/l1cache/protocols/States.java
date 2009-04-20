/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols;

/**
 *
 * @author amit
 */
public class States {

    public String[] stateNames;

    public States(String[] states) {
        stateNames = states;
    }

    public String getState(int index) {
        return stateNames[index];
    }
}
