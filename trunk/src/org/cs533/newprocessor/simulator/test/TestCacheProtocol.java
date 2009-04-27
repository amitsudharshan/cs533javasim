/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator.test;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.memorysubsystem.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.MIProtocol;
import org.cs533.newprocessor.components.memorysubsystem.MainMemory;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class TestCacheProtocol {

    static final int FIRST_STORE_VALUE = 35232;

    public static void main(String[] args) {
        try {
            //instantiate and register all clients
            MainMemory m = new MainMemory();
            CacheCoherenceBus<MIProtocol.MIBusMessage> bus = new CacheCoherenceBus<MIProtocol.MIBusMessage>(m);
            L1Cache l1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            bus.registerClient(l1);

            //start the simulation
            Simulator.runSimulation();

            //execute the memory instructions
            MemoryInstruction store = MemoryInstruction.Store(0, AbstractInstruction.intToByteArray(FIRST_STORE_VALUE));
            l1.enqueueMemoryInstruction(store);
            MemoryInstruction load = MemoryInstruction.Load(0);
            l1.enqueueMemoryInstruction(load);

            // wait till they are finished
            while (!store.getIsCompleted() && !load.getIsCompleted()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TestCacheProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //stop the simulator

            Simulator.stopSimulation();
//verify result:
            boolean isSuccess = true;
            System.out.println("for memory address 0x" + Integer.toHexString(store.getInAddress()) + " we stored " + FIRST_STORE_VALUE);
            System.out.println("for memory address 0x " + Integer.toHexString(load.getInAddress()) + " we returned " + AbstractInstruction.byteArrayToInt(load.getOutData()));
            if (!Arrays.equals(load.getOutData(), store.getInData())) {
                System.out.println("FAILURE");
                isSuccess = false;
            }
            if (isSuccess) {
                System.out.println("SUCCESS!");
            }
        } catch (Exception ex) {
            System.out.println("FAILURE IN EXCEPTION WITH TRACE:");
            ex.printStackTrace();
            Simulator.stopSimulation();
        }
    }
}
