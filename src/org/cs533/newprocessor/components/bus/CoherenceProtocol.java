/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cs533.newprocessor.components.bus;

import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.components.memorysubsystem.LineState;

/**
 *
 * @author brandon
 */
public interface CoherenceProtocol <BusMessage,LineStates extends LineState>
       extends BusClient<BusMessage>, ComponentInterface {
    public void setContext(ProtocolContext<LineStates> context);
}
