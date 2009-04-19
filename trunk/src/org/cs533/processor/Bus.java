/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.processor;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author Vivek
 */
public class Bus extends AbstractComponent {

    public ArrayList<CoherenceMessage> enRouteMessages;
    private static int unUsedMessageID = 0;
    private static final int BANDWIDTH = 64;
    private static final int LATENCY = 5;

    public Bus() {
        super(BANDWIDTH, LATENCY);
        enRouteMessages = new ArrayList<CoherenceMessage>();
    }

    /**
     * addMesage will add a message to the bus
     * @param The CoherenceMessage to add
     */
    public void addMessage(CoherenceMessage e) {
        enRouteMessages.add(e);
    }

    public int getMessageStatus(int messageID) {
        CoherenceMessage cm = null;
        if (enRouteMessages.isEmpty()) {
            System.out.println("no messages on bus.");
            return -1;
        } else {
            ListIterator<CoherenceMessage> iter = enRouteMessages.listIterator();
            //not quite sure how to use
            if (iter.next() != null) {
                cm = iter.previous();
                if (cm.getMessageID() == messageID) {
                    return cm.getMessageStatus();
                }

            }

            while (iter.hasNext()) {
                cm = iter.next();
                if (cm.getMessageID() == messageID) {
                    return cm.getMessageStatus();
                }
            }
        }

        return -1;
    }

    public static int generateUniqueMessageID() {
        int myID = unUsedMessageID;
        unUsedMessageID++;
        return myID;
    }

    public ArrayList<CoherenceMessage> getBusSnapshot() {
        return new ArrayList<CoherenceMessage>(enRouteMessages);
    }

    public boolean isAvailable() {
        if (enRouteMessages.isEmpty()) {
            System.out.println("The bus is available.");
            return true;
        } else {
            System.out.println("The bus is not available.");
            return false;
        }


    }

    //arbitration  when many messages need the bus at the same time
    //
    public boolean performBusArbitration() {

        return false;
    }
}
