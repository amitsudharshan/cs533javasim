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

    public synchronized T get(int address) {
        return lines.get(address);
    }

    /** Try to find and remove an evictable line
     */
    protected boolean silentEvict(int address) {
       T evicted = null;
        for (T line: lines.values()) {
            if (line.silentlyEvictable() && line.address != address) {
                evicted = line;
                break;
            }
        }
        if (evicted != null) {
            lines.remove(evicted.address);
            return true;
        } else {
            return false;
        }
    }

    protected T evict(int address) {
        if (silentEvict(address)) {
            return null;
        }
        for (T line: lines.values()) {
            if (line.address != address) {
                return line;
            }
        }
        return null;
    }

    public synchronized T reserveSpace(int address) {
        if (lines.size() >= size-1) {
            return evict(address);
        } else {
            return null;
        }
    }

    /**
     * Add a cache line to the hash table. Must not be a line already
     * in the hash table. May return a line that needs
     * to be evicted. You must call evictionCompleted once the eviction is
     * done (line written back to memory, or passed to another cache),
     * and must not call add again until the eviction is done.
     * @param newLine
     * @return An evicted line, or null
     */
    public synchronized T add(T newLine) {
        assert !lines.containsKey(newLine.address);
        lines.put(newLine.address, newLine);
        return reserveSpace(newLine.address);
    }

    public synchronized boolean addNoEvict(T newLine) {
        assert !lines.containsKey(newLine.address);
        if (lines.size() >= size-1) {
            // try to evict a silent line
            if (!silentEvict(newLine.address)) {
                return false;
            }
        }
        lines.put(newLine.address, newLine);
        return true;
    }
}
