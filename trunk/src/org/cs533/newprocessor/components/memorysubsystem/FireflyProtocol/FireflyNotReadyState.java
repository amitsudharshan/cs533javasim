package org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class FireflyNotReadyState extends FireflyCacheControllerState {

    public FireflyNotReadyState(FireflyCacheController controller) {
        super(controller);
    }

    @Override
    public StateAnd<FireflyBusMessage, CacheControllerState<FireflyBusMessage>> recieveBroadcastMessage(FireflyBusMessage b) {
        return noJump(handleBroadcastMessage(b));
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<FireflyBusMessage>> recieveClientRequest(MemoryInstruction request) {
        CacheLine<FireflyLineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<FireflyLineState>(request.getInAddress(), null, FireflyLineState.INVALID);
            if (!controller.data.addNoEvict(line)) {
                // have to do a transaction to evict
                return jumpTo(new FireflyReadyState(request, controller));
            }
        }
        return handleClientRequestAsMemory(request, line);
    }
}
