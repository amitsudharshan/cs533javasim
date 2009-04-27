/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author amit
 */
public class LRUEvictHashTable<T extends CacheLine> {
    
    LinkedHashMap<Integer,T> lines;

    int size;

    public LRUEvictHashTable(int size) {
        lines = new LinkedHashMap<Integer,T>(size+size/2, (float)0.75, true);
        this.size = size;
    }

    T get(int address) {
        return lines.get(address);
    }

    T add(T newLine) {
        lines.put(newLine.address, newLine);
        if (lines.size() == size) {
            Iterator<Entry<Integer,T>> iter = lines.entrySet().iterator();
            Entry<Integer,T> removed = iter.next();
            lines.remove(removed.getKey());
            return removed.getValue();
        } else {
            return null;
        }
    }
}
