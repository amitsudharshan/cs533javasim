package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class MESINotReadyState extends MESICacheControllerState {

    public MESINotReadyState(MESICacheController controller) {
        super(controller);
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        logger.debug("recieveBroadcastMessage("+b+")");
        StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> action = handleBroadcastMessage(b);
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<MESIBusMessage>> recieveClientRequest(MemoryInstruction request) {
        logger.debug("recieveClientRequest");
        CacheLine<MESILineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<MESILineState>(request.getInAddress(), null, MESILineState.INVALID);
            CacheLine<MESILineState> evicted = controller.data.add(line);
             if (evicted != null && evicted.state == MESILineState.MODIFIED) {
                logger.debug("line evicted");
                return jumpTo(new MESIReadyState(MESIBusMessage.Writeback(evicted.address, evicted.data), request, controller));
            }
        }
        return handleClientRequestAsMemory(request, line);
    }
}
