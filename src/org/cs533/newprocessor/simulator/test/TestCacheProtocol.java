/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator.test;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.assembler.abstractandinterface.AbstractInstruction;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.memorysubsystem.CacheController;
import org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol.FireflyBusMessage;
import org.cs533.newprocessor.components.memorysubsystem.FireflyProtocol.FireflyCacheController;
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
        doTest(FireflyBusMessage.class, FireflyCacheController.class);
    }
    
    public static <Msg extends AbstractBusMessage<Msg>,
                    Cache extends CacheController<Msg>>
                    void doTest(Class<Msg> _, Class<Cache> cls) {
        Logger.getRootLogger().setLevel(Level.ALL);;
        BasicConfigurator.configure();
        try {
            //instantiate and register all clients
            MemoryInterface m = new MainMemory();
            // CacheCoherenceBus<MIBusMessage> bus = new CacheCoherenceBus<MIBusMessage>(m);
            CacheCoherenceBus<Msg> bus = new CacheCoherenceBus<Msg>(m);
            //L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>
            //        firstL1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            //L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>
            //        secondL1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            Cache firstL1 = cls.getConstructor(int.class).newInstance(1);
            Cache secondL1 = cls.getConstructor(int.class).newInstance(2);

            bus.registerClient(firstL1);
            bus.registerClient(secondL1);

            //queue the memory instructions
            MemoryInstruction storeFirst = MemoryInstruction.Store(0, AbstractInstruction.intToByteArray(FIRST_STORE_VALUE));
            firstL1.enqueueMemoryInstruction(storeFirst);
            MemoryInstruction storeSecond = MemoryInstruction.Store(1, AbstractInstruction.intToByteArray(SECOND_STORE_VALUE));
            firstL1.enqueueMemoryInstruction(storeSecond);
            MemoryInstruction loadFirst = MemoryInstruction.Load(0);
            firstL1.enqueueMemoryInstruction(loadFirst);
            MemoryInstruction loadSecond = MemoryInstruction.Load(1);
            firstL1.enqueueMemoryInstruction(loadSecond);

            //start the simulation
            Simulator.runSimulation();

            // wait till they are finished
            while (!storeFirst.getIsCompleted() || !loadFirst.getIsCompleted() || !loadFirst.getIsCompleted() || !loadSecond.getIsCompleted()) {
                Thread.sleep(10);
            }

            Simulator.stopSimulation();
            //verify result:
            boolean isSuccess = true;
            System.out.println("for memory address 0x0 we should have stored "+Integer.toString(FIRST_STORE_VALUE));
            System.out.println("for memory address 0x" + Integer.toHexString(storeFirst.getInAddress()) + " we stored " + AbstractInstruction.byteArrayToInt(storeFirst.getInData()));
            System.out.println("for memory address 0x" + Integer.toHexString(loadFirst.getInAddress()) + " we returned " + AbstractInstruction.byteArrayToInt(loadFirst.getOutData()));
            System.out.println("for memory address 0x1 we should have stored "+Integer.toString(SECOND_STORE_VALUE));
            System.out.println("for memory address 0x" + Integer.toHexString(storeSecond.getInAddress()) + " we stored " + AbstractInstruction.byteArrayToInt(storeSecond.getInData()));
            System.out.println("for memory address 0x" + Integer.toHexString(loadSecond.getInAddress()) + " we returned " + AbstractInstruction.byteArrayToInt(loadSecond.getOutData()));

            Simulator.printStatistics();
        } catch (Exception ex) {
            System.out.println("FAILURE IN EXCEPTION WITH TRACE:");
            ex.printStackTrace();
            Simulator.stopSimulation();
        }
    }
}
