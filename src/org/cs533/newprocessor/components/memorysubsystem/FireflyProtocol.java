/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.memorysubsystem;

/**
 *
 * @author Vivek
 */



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.cs533.newprocessor.components.bus.BusAggregator;
import org.cs533.newprocessor.components.bus.CoherenceProtocol;
import org.cs533.newprocessor.components.bus.ProtocolContext;

/**
 *
 * @author brandon
 */
public class FireflyProtocol
        implements CoherenceProtocol<FireflyProtocol.FireflyBusMessage, FireflyProtocol.FireflyLineState> {

    ProtocolContext<FireflyLineState> context;
    MemoryInstruction pendingRequest;
    LRUEvictHashTable<CacheLine<FireflyLineState>> data;

    /** If state is Ready, pendingRequest is non-null */

    private enum ProtocolState
    {
        RunningTransaction, FinishedRequest, GettingRequest, Ready, Uninitialized
    }
    ProtocolState state;

    void FireflyProtocol()
    {
        state = ProtocolState.Uninitialized;
    }

    public enum FireflyLineState
    {
        MODIFIED, EXCLUSIVE, SHARED
    }



    public enum FireflyBusMessageType
    {
       UPDATE
    }


    public class FireflyBusMessage
     {
        public final FireflyBusMessageType type;
        public final int address;

        public FireflyBusMessage(FireflyBusMessageType type, int address)
        {
            this.type = type;
            this.address = address;
        }
     }



    public void setContext(ProtocolContext<FireflyLineState> c)
    {
        context = c;
        data = context.getData();
        state = ProtocolState.GettingRequest;
    }

    public void recieveMessage(FireflyBusMessage msg)
    {
        int evictAddress = msg.address;
        CacheLine line = data.get(evictAddress);
        if (line != null)
        {
            line.state = FireflyLineState.SHARED;
        }
    }

    public BusAggregator<FireflyBusMessage> getAggregator()
    {
        return null;
    }

    public FireflyBusMessage getResponse()
    {   
        return null;
    }

    public FireflyBusMessage getBusMessage()
    {
        if (state == ProtocolState.Ready) {
            state = ProtocolState.RunningTransaction;
            return new FireflyBusMessage(FireflyBusMessageType.UPDATE, pendingRequest.inAddress);
        } else {
            return null;
        }
    }

    public MemoryInstruction getMemoryRequest()
    {
        if (pendingRequest != null)
        {
            return pendingRequest;
        }
        else
        {
            return null;
        }
    }

    public void recieveMemoryResponse(MemoryInstruction resp) {
        if (state == ProtocolState.RunningTransaction) {
            state = ProtocolState.FinishedRequest;
            pendingRequest = null;
        }
    }

    public void runPrep()
    {
        if (state == ProtocolState.GettingRequest)
        {
            assert(pendingRequest == null);
            pendingRequest = context.getNextRequest();
        }
    }

    
    public void runClock()
    {
        if (state == ProtocolState.FinishedRequest)
        {
            state = ProtocolState.GettingRequest;
        } 
        else if (state == ProtocolState.GettingRequest && pendingRequest != null)
        {
            CacheLine<FireflyLineState> line = data.get(pendingRequest.getInAddress());
            if (line != null && line.state == FireflyLineState.MODIFIED) {
                // handle request out of cache
                switch (pendingRequest.getType())
                {
                    case Load:
                        pendingRequest.setOutData(line.data);
                    case Store:
                        line.data = pendingRequest.getInData();
                    case CAS:
                        if (line.data == pendingRequest.compareData)
                        {
                            pendingRequest.outData = line.data;
                            line.data = pendingRequest.inData;
                        }
                }

                pendingRequest.setIsCompleted(true);
                // go on to the next request;
                pendingRequest = null;
            }
            else
            {
                // must go to bus
                state = ProtocolState.Ready;
            }
        }
    }

    public int getLatency()
    {
        return 1;
    }
}
