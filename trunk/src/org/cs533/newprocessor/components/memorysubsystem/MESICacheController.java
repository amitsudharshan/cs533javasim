/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.components.memorysubsystem;

import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.CacheControllerState;
import org.cs533.newprocessor.components.bus.StateAnd;
import org.cs533.newprocessor.components.memorysubsystem.MESICacheController.MESIBusMessage;

/**
 *
 * @author amit
 */
public class MESICacheController extends CacheController<MESIBusMessage> {

    int cacheID;

    public static enum MESIBusMessageTypes {
        
    }
    public static class MESIBusMessage extends AbstractBusMessage<MESIBusMessage> {
    }

    public static enum MESILineState {

        MODIFIED, EXCLUSIVE, SHARED, INVALID;
    }
    final LRUEvictHashTable<CacheLine<MESILineState>> data;

    MESICacheController(int cacheID_) {
        super();
        cacheID = cacheID_;
        data = new LRUEvictHashTable<CacheLine<MESILineState>>(Globals.L1_SIZE_IN_NUMBER_OF_LINES);
        state = new NotReadyState();
    }

    public class ReadyState extends CacheControllerState<MESIBusMessage> {

        final MemoryInstruction pendingRequest;

        ReadyState(MemoryInstruction request) {
            pendingRequest = request;
        }
    }

    public class NotReadyState extends CacheControllerState<MESIBusMessage> {

        @Override
        public StateAnd<MemoryInstruction, CacheControllerState<MESIBusMessage>> recieveClientRequest(MemoryInstruction request) {
            if (/*can handle locally*/true) {
                request.setOutData(/* actual response data */null);
                return sameState(request);
            } else {
                return jump(new ReadyState(request));
            }
        }
    }
}
