package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.memorysubsystem.LineState;

public enum FireflyLineState implements LineState {

    MODIFIED, EXCLUSIVE, SHARED, INVALID;

    public boolean silentlyEvictable() {
        return this != MODIFIED;
    }
}
