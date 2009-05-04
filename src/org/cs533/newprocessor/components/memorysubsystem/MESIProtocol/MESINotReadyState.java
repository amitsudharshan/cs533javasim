package org.cs533.newprocessor.components.memorysubsystem.MESIProtocol;

import java.util.Arrays;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.CacheLine;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

public class MESINotReadyState extends MESICacheControllerState {

    MESICacheController controller;

    public MESINotReadyState(MESICacheController controller) {
        super(controller);
    }

    @Override
    public StateAnd<MESIBusMessage, CacheControllerState<MESIBusMessage>> recieveBroadcastMessage(MESIBusMessage b) {
        return handleBroadcastMessage(b);
    }

    @Override
    public StateAnd<MemoryInstruction, CacheControllerState<MESIBusMessage>> recieveClientRequest(MemoryInstruction request) {
        // FIXME - do we have to install an invalid line here?
        CacheLine<MESILineState> evicted = controller.data.reserveSpace(request.getInAddress());
        if (evicted != null) {
            return jumpTo(new MESIReadyState(MESIBusMessage.Writeback(evicted.address, evicted.data), request, controller));
        }
        CacheLine<MESILineState> line = controller.data.get(request.getInAddress());
        return handleClientRequestAsMemory(request, line);
    }
}
