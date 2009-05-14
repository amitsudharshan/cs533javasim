/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author brandon
 */
public interface LineState {
    /**
     * True if it's okay to discard a line in this state
     * without ensuring it's state is written back to main memory
     * @return
     */
    public boolean silentlyEvictable();
}
