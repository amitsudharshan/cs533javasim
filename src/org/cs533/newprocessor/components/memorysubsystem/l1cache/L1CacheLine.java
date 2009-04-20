/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache;

import org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols.AbstractProtocol;

/**
 *
 * @author amit
 */
public class L1CacheLine {

    public byte[] data;
    public boolean isDirty;
    public int currentState;

    public L1CacheLine(byte[] data, boolean isDirty, int state) {
        this.data = data;
        this.isDirty = isDirty;
        currentState = state;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getCurrentState() {
        return currentState;
    }

    public boolean isIsDirty() {
        return isDirty;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }
}
