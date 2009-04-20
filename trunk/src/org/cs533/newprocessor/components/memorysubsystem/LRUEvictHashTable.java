/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.components.memorysubsystem.l2cache.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.cs533.newprocessor.Globals;

/**
 *
 * @author amit
 */
public class LRUEvictHashTable extends LinkedHashMap {

    public Integer address = -1;
    public L2CacheLine line = null;
    int size = Globals.L2_SIZE_IN_NUMBER_OF_LINES;

    public LRUEvictHashTable(int size_) {
        super(size_, 1, true);
        size = size_;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry entry) {
        if (size() > size) {
            address = (Integer) entry.getKey();
            line = (L2CacheLine) entry.getValue();
            return true;
        } else {
            return false;
        }
    }

    public void resetRemoved() {
        address = -1;
        line = null;
    }
}
