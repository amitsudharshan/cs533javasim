/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;

/**
 * A client on a cache coherence bus. Implements a generic broadcast protocol:
 * 
 * During the {@link org.cs533.newprocessor.components.ComponentInterface#runPrep runPrep}
 * phase a client is selected to run the next transaction by calling
 * <code>getBusMessage</code> until it gets a non-null message.
 * 
 * If so, it delivers this message to all other clients in the same phase,
 * by calling <code>recieveMessage</code>.
 * It determines whether the bus master wants to aggregate a response
 * or send a memory request to the higher level by checking if
 * the <code>getAggregator</code> or <code>getMemoryRequest</code>
 * accessors are non-null.
 * 
 * If a response is needed the bus checks the <code>getResponse</code>
 * property of each client for as many cycles as it takes until they
 * have all produced a response, which are aggregated through the
 * provided aggregator. Once the response is available it is delivered
 * to all clients with <code>recieveMessage</code>. The next clock
 * the bus owner's <code>getAggregator</code> and <code>getMemoryRequest</code>
 * accessors are checked to see if we require another communication round
 * or a memory request, if neither the round is done and we can look
 * for a new bus owner.
 * 
 * If a memory request is made the client propagates it to the
 * upstream interface, waits as long as it takes until the request
 * is finished, and then presents the filled-out object to all
 * clients (to allow read snarfing and so on) through the
 * <code>recieveMemoryResponse</code> accessor. As with communication rounds,
 * on the next clock the bus polls the current owner to see if another
 * communcation round or memory request is required.
 * @author brandon
 */
public interface BusClient<BusMessage> {
    // you are not allowed to change the value returned by the getters
    // when this is called (or, as usual, for any other reason).
    // May be called before or after your own runPrep - your state
    // at the end of runPrep may not depend on this order.
    // Will be called at most once per runPrep by a bus.
    void recieveMessage(BusMessage msg);
    BusMessage getResponse();
    BusMessage getBusMessage();
    // Ownership of the aggregator is given to the bus, which will call
    // it unpredictably.
    BusAggregator<BusMessage> getAggregator();
    MemoryInstruction getMemoryRequest();
    // Will be called at most once per runPrep by a bus.
    // May be called before or after your own runPrep - your state
    // at the end of runPrep may not depend on this order.
    void recieveMemoryResponse(MemoryInstruction resp);
};
