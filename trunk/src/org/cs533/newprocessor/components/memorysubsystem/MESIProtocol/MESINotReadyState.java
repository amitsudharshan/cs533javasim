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
        MESICacheController.logger.debug("recieveBroadcastMessage");
        StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> action = handleBroadcastMessage(b);
        MESICacheController.logger.debug(action.toString());
        return action;
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<MESIBusMessage>> recieveClientRequest(MemoryInstruction request) {
        CacheLine<MESILineState> line = controller.data.get(request.getInAddress());
        if (line == null) {
            line = new CacheLine<MESILineState>(request.getInAddress(), null, MESILineState.INVALID);
            CacheLine<MESILineState> evicted = controller.data.add(line);
             if (evicted != null) {
                return jumpTo(new MESIReadyState(MESIBusMessage.Writeback(evicted.address, evicted.data), request, controller));
            }
        }
        return handleClientRequestAsMemory(request, line);
    }
}
