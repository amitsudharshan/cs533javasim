package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import org.cs533.newprocessor.components.memorysubsystem.LineState;

public enum HybridLineState implements LineState {

    MODIFIED, EXCLUSIVE, SHARED, INVALID;

    public boolean silentlyEvictable() {
        return this != MODIFIED;
    }
}
