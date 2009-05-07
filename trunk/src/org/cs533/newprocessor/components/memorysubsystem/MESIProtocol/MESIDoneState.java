/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class MESIDoneState extends MESICacheControllerState {
    MemoryInstruction response;

    public MESIDoneState(MemoryInstruction response, MESICacheController controller) {
        super(controller);
        this.response = response;
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b+")");
        StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> action = handleBroadcastMessage(b);
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<MESIBusMessage>> pollRequestStatus(MemoryInstruction request) {
        logger.debug("pollRequestStatus");
        return andJump(response, new MESINotReadyState(controller));
    }
}
