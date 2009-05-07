package org.cs533.newprocessor.components.memorysubsystem.Hybrid;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class HybridNotReadyState extends HybridCacheControllerState {

    public HybridNotReadyState(HybridCacheController controller) {
        super(controller);
    }

    @Override
    public StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> recieveBroadcastMessage(HybridBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b+")");
        StateAnd<HybridBusMessage, CacheControllerState<HybridBusMessage>> action = handleBroadcastMessage(b);
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<HybridBusMessage>> recieveClientRequest(MemoryInstruction request) {
        logger.debug("recieveClientRequest");
        CacheLine<HybridLineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<HybridLineState>(request.getInAddress(), null, HybridLineState.INVALID);
            CacheLine<HybridLineState> evicted = controller.data.add(line);
             if (evicted != null && evicted.state == HybridLineState.MODIFIED) {
                logger.debug("line evicted");
                return jumpTo(new HybridReadyState(HybridBusMessage.Writeback(evicted.address, evicted.data), request, controller));
            }
        }
        return handleClientRequestAsMemory(request, line);
    }
}
