/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem.l1cache.protocols;

import org.cs533.newprocessor.components.memorysubsystem.l1cache.L1CacheLine;

/**
 *
 * @author amit
 */
public class ModifyInvalidProtocol extends AbstractProtocol {

    @Override
    public void handleMessage(BusMessage message, L1CacheLine line) {
        if (line == null) {
            message.setResponse(0);
        } else {
            message.setResponse(1);
        }
    }

    public enum MIStates {

        MODIFIED, INVALID
    }

    public ModifyInvalidProtocol() {
        super(new String[]{"MODIFIED", "INVALID"});
    }

    @Override
    public BusMessage getNextState(L1CacheLine line, CACHE_STATUS status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
