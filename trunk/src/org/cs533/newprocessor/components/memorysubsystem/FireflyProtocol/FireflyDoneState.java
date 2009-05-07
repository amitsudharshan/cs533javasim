/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 *
 * @author amit
 */
public class FireflyDoneState extends FireflyCacheControllerState {
    MemoryInstruction response;

    public FireflyDoneState(MemoryInstruction response, FireflyCacheController controller) {
        super(controller);
        this.response = response;
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> recieveBroadcastMessage(FireflyBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b+")");
        StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> action = handleBroadcastMessage(b);
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<FireflyBusMessage>> pollRequestStatus(MemoryInstruction request) {
        logger.debug("pollRequestStatus");
        return andJump(response, new FireflyNotReadyState(controller));
    }
}
