package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.memorysubsystem.LineState;

public enum MESILineState implements LineState {
    MODIFIED, EXCLUSIVE, SHARED, INVALID;

    public boolean silentlyEvictable() {
        return this != MODIFIED;
    }
}
