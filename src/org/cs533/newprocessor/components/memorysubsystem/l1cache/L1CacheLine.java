/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import org.cs533.newprocessor.components.memorysubsystem.CacheLine;

/**
 *
 * @author amit
 */
public class L1CacheLine extends CacheLine {

    public enum L1LineStates {

        Valid, Dirty_Valid, Invalid,INVALIDATE_AFTER_WRITE_BACK
    }
    public L1LineStates currentState;

    public L1CacheLine(byte[] data, boolean isDirty, L1LineStates state) {
        this.data = data;
        currentState = state;
    }

    public void setCurrentState(L1LineStates state) {
        currentState = state;
    }

    public L1LineStates getCurrentState() {
        return currentState;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
