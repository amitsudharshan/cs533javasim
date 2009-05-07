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
        logger.debug("recieveBroadcastMessage("+b+")");
        return handleBroadcastMessage(b);
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<FireflyBusMessage>> recieveClientRequest(MemoryInstruction request) {
        logger.debug("recieveClientRequest");
        CacheLine<FireflyLineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<FireflyLineState>(request.getInAddress(), null, FireflyLineState.INVALID);
            CacheLine<FireflyLineState> evicted = controller.data.add(line);
             if (evicted != null && evicted.state == FireflyLineState.MODIFIED) {
                logger.debug("line evicted");
                return jumpTo(new FireflyReadyState(FireflyBusMessage.DirtyAckWriteback(evicted.address, evicted.data), request, controller));
            }
        }
        return handleClientRequestAsMemory(request, line);
    }
}
