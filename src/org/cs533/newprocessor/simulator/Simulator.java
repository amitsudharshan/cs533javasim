/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;
import org.cs533.newprocessor.AsyncComponentInterface;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.Assembler;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.core.ProcessorCore;
import org.cs533.newprocessor.components.memorysubsystem.L1Cache;
import org.cs533.newprocessor.components.memorysubsystem.MIProtocol;
import org.cs533.newprocessor.components.memorysubsystem.MainMemory;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;

/**
 *
 * @author amit
 */
public class Simulator {

    static ArrayList<AsyncComponentInterface> components = new ArrayList<AsyncComponentInterface>();
    static final HashMap<String, Integer> eventCounter = new HashMap<String, Integer>();
    static Thread simulatorThread;
    static boolean isRunning = false;

    private Simulator() {
    }

    public static byte[] generateSimpleCacheLineFromOffset(int offset) {

        byte[] b = new byte[Globals.CACHE_LINE_SIZE];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) offset++;
        }
        return b;
    }

    public static void logEvent(String event) {
        synchronized (eventCounter) {
            // System.out.println(event);
            Integer count = eventCounter.get(event);
            if (count == null) {
                count = new Integer(1);
            } else {
                count = new Integer(count + 1);
            }
            eventCounter.put(event, count);
            System.out.println("log: " + event);
        }
    }

    public static void printStatistics() {
        synchronized (eventCounter) {
            for (String key : eventCounter.keySet()) {
                System.out.println("For event: " + key + " count is " + eventCounter.get(key));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String asmFileName = "/home/amit/NetBeansProjects/cs533javasim/src/org/cs533/asm/cands.asm";
        if (args.length > 0) {
            asmFileName = args[0];
        }
        //   ExecutableImage exec = ExecutableImage.loadImageFromFile(imageFileName);
        ExecutableImage exec = Assembler.getFullImage(asmFileName);
        MemoryInterface m = new MainMemory(exec.getMemoryImage());
        CacheCoherenceBus<MIProtocol.MIBusMessage> bus = new CacheCoherenceBus<MIProtocol.MIBusMessage>(m);
        int[] pcStart = exec.getInitialPC();
        ProcessorCore[] pCore = new ProcessorCore[pcStart.length];
        for (int i = 0; i < pCore.length; i++) {
            MemoryInterface l1 = new L1Cache<MIProtocol.MIBusMessage, MIProtocol.MILineState, MIProtocol>(new MIProtocol());
            bus.registerClient((L1Cache) l1);
            pCore[i] = new ProcessorCore(pcStart[i], l1, i);
        }
        runSimulation();
        int doneProcessor = 0;
        while (doneProcessor < pCore.length) {
            if (pCore[doneProcessor].isHalted()) {
                doneProcessor++;
            }
            Thread.sleep(10);
        }
        stopSimulation();
    }

    public static void registerComponent(ComponentInterface component) {
        if (isRunning) {
            throw new RuntimeException("Attempted to add component with simulation running");
        }
        components.add(new ComponentWrapper(component));
    }

    public static void registerComponent(AsyncComponentInterface component) {
        if (isRunning) {
            throw new RuntimeException("Attempted to add component with simulation running");
        }
        components.add(component);
    }

    public static void stopSimulation() {
        simulatorThread.interrupt();
        try {
            simulatorThread.join();
        } catch (InterruptedException e) {
            return;
        }
    }

    private static class ComponentWrapper implements AsyncComponentInterface {

        CyclicBarrier prepBarrier, clockBarrier;
        ComponentInterface component;

        public ComponentWrapper(ComponentInterface c) {
            this.component = c;
        }

        public void configure(CyclicBarrier prepBarrier, CyclicBarrier clockBarrier) {
            this.prepBarrier = prepBarrier;
            this.clockBarrier = clockBarrier;
        }

        public void run() {
            try {
                while (true) {
                    prepBarrier.await();
                    component.runPrep();
                    clockBarrier.await();
                    component.runClock();
                }
            } catch (InterruptedException e) {
                // how workers are killed
            } catch (BrokenBarrierException b) {
                // what other blocked workers see when one is killed
                // while one is waiting at barrier
            }
        }
    }

    public static void runSimulation() {
        isRunning = true;
        final Thread[] workers = new Thread[components.size()];
        final CyclicBarrier prepStart = new CyclicBarrier(components.size());
        final CyclicBarrier clockStart = new CyclicBarrier(components.size());
        for (int i = 0; i < workers.length; i++) {
            AsyncComponentInterface r = components.get(i);
            r.configure(prepStart, clockStart);
            workers[i] = new Thread(r);
            workers[i].start();
        }
        simulatorThread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Long.MAX_VALUE);
                    }
                } catch (InterruptedException ex) {
                    Logger.getAnonymousLogger().info("recieved interrupt probably caused by stop call");
                }
                for (int i = 0; i < workers.length; ++i) {
                    workers[i].interrupt();
                }
                try {
                    for (int i = 0; i < workers.length; ++i) {
                        workers[i].join();
                    }
                } catch (InterruptedException ex) {
                }
                isRunning = false;
            }
        });
        simulatorThread.start();
    }
}
