/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator.test;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.memorysubsystem.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.MIProtocol;
import org.cs533.newprocessor.components.memorysubsystem.MainMemory;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInstruction;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;
import org.cs533.newprocessor.simulator.Simulator;

/**
 *
 * @author amit
 */
public class TestCacheProtocol {

    static final int FIRST_STORE_VALUE = 35232;
    static final int SECOND_STORE_VALUE = 52342;
    static Logger logger = Logger.getLogger(TestCacheProtocol.class.getName());

    public static void main(String[] args) {

        Logger.getRootLogger().setLevel(Level.INFO);
        BasicConfigurator.configure();
        try {
            //instantiate and register all clients
            MemoryInterface m = new MainMemory();
            CacheCoherenceBus<MIProtocol.MIBusMessage> bus = new CacheCoherenceBus<MIProtocol.MIBusMessage>(m);
            MemoryInterface firstL1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            MemoryInterface secondL1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            bus.registerClient((L1Cache) firstL1);
            bus.registerClient((L1Cache) secondL1);

            //start the simulation
            Simulator.runSimulation();

            //execute the memory instructions
            MemoryInstruction storeFirst = MemoryInstruction.Store(0, AbstractInstruction.intToByteArray(FIRST_STORE_VALUE));
            firstL1.enqueueMemoryInstruction(storeFirst);
            MemoryInstruction loadFirst = MemoryInstruction.Load(0);
            firstL1.enqueueMemoryInstruction(loadFirst);
            MemoryInstruction loadSecond = MemoryInstruction.Load(0);
            firstL1.enqueueMemoryInstruction(loadSecond);


            // wait till they are finished
            while (!storeFirst.getIsCompleted() || !loadFirst.getIsCompleted() || !loadSecond.getIsCompleted()) {
                Thread.sleep(10);
            }

            Simulator.stopSimulation();
//verify result:
            boolean isSuccess = true;
            System.out.println("for memory address 0x" + Integer.toHexString(storeFirst.getInAddress()) + " we stored " + FIRST_STORE_VALUE);
            System.out.println("for memory address 0x" + Integer.toHexString(loadFirst.getInAddress()) + " we returned " + AbstractInstruction.byteArrayToInt(loadFirst.getOutData()));

            Simulator.printStatistics();
        } catch (Exception ex) {
            System.out.println("FAILURE IN EXCEPTION WITH TRACE:");
            ex.printStackTrace();
            Simulator.stopSimulation();
        }
    }
}
