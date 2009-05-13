/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cs533.newprocessor.simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cs533.newprocessor.AsyncComponentInterface;
import org.cs533.newprocessor.ComponentInterface;
import org.cs533.newprocessor.Globals;
import org.cs533.newprocessor.assembler.Assembler;
import org.cs533.newprocessor.components.bus.AbstractBusMessage;
import org.cs533.newprocessor.components.bus.CacheCoherenceBus;
import org.cs533.newprocessor.components.core.ProcessorCore;
import org.cs533.newprocessor.components.memorysubsystem.CacheController;
import org.cs533.newprocessor.components.memorysubsystem.MESIProtocol.MESIBusMessage;
import org.cs533.newprocessor.components.memorysubsystem.MESIProtocol.MESICacheController;
import org.cs533.newprocessor.components.memorysubsystem.MainMemory;
import org.cs533.newprocessor.components.memorysubsystem.MemoryInterface;
import org.cs533.newprocessor.components.memorysubsystem.NoisyCacheCoherenceState;
import org.cs533.newprocessor.simulator.GUI.MainSimulatorFrame;

/**
 *
 * @author amit
 */
public class Simulator {

    static ArrayList<AsyncComponentInterface> components = new ArrayList<AsyncComponentInterface>();
    static Logger logger = Logger.getLogger(Simulator.class.getName());
    static final HashMap<String, Integer> eventCounter = new HashMap<String, Integer>();
    public static Thread simulatorThread;
    public static boolean isRunning = false;
    static final Object tickLock = new Object();
    static boolean LAUNCH_GUI = false;
    static boolean TRACE = false;

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.INFO);
        // Logger.getLogger("CacheController").setLevel(Level.DEBUG);
        //    Logger.getLogger(ProcessorCore.class).setLevel(Level.DEBUG);
        BasicConfigurator.configure();
        String example = "pmatrixmultiply.asm";
        String asmFileName = new File(new File(System.getProperty("user.dir")).toURI().resolve("src/org/cs533/asm/" + example)).toString();
        System.out.println(asmFileName);
        if (args.length > 0) {
            asmFileName = args[0];
        }
        // doTest(FireflyBusMessage.class, FireflyCacheController.class, asmFileName);
        doTest(MESIBusMessage.class, MESICacheController.class, asmFileName);
    }

    public static void incrementClockTicks(int incrementBy) {
        synchronized (tickLock) {
            clockCount += incrementBy;
        }
    }

    public static void incrementPrepTicks(int incrementBy) {
        synchronized (tickLock) {
            prepCount += incrementBy;
        }
    }

    static <Msg extends AbstractBusMessage<Msg>, Cache extends CacheController<Msg>> void doTest(Class<Msg> _, Class<Cache> cache, String asmFileName) throws Exception {
        //   ExecutableImage exec = ExecutableImage.loadImageFromFile(imageFileName);
        ExecutableImage exec = Assembler.getFullImage(asmFileName);
        MemoryInterface m = new MainMemory(exec.getMemoryImage());
        CacheCoherenceBus<Msg> bus = new CacheCoherenceBus<Msg>(m);
        int[] pcStart = exec.getInitialPC();
        ProcessorCore[] pCore = new ProcessorCore[pcStart.length];
        for (int i = 0; i < pCore.length; i++) {
            Cache l1 = cache.getConstructor(int.class).newInstance(i);
            bus.registerClient(l1);
            if (TRACE) {
                l1.setState(new NoisyCacheCoherenceState(l1.getState()));
            }
            pCore[i] = new ProcessorCore(pcStart[i], l1, i);
            if (LAUNCH_GUI) {
                MainSimulatorFrame.getFrameAndShow(pCore[i]).spawnUpdatingThread();

            }
        }
        if (!LAUNCH_GUI) {
            runSimulation();
        }
        int doneProcessor = 0;
        while (doneProcessor < pCore.length) {
            if (pCore[doneProcessor].isHalted()) {
                doneProcessor++;
            }
            Thread.sleep(10);
        }
        stopSimulation();
        printStatistics();
    }

    public static String getPhase() {
        if (!isRunning) {
            return "Not Started, preps " + Integer.toString(prepCount) + ", clocks " + Integer.toString(clockCount);
        }
        if (inPrep) {
            return "Prep" + Integer.toString(prepCount);
        } else {
            return "Clock" + Integer.toString(clockCount);
        }
    }
    static boolean inPrep = true;
    static int prepCount = 0;
    static int clockCount = -1;

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
            Integer count = eventCounter.get(event);
            if (count == null) {
                count = new Integer(1);
            } else {
                count = new Integer(count + 1);
            }
            eventCounter.put(event, count);
            logger.debug("log: " + event);
        }
    }

    public static void printStatistics() {
        synchronized (eventCounter) {
            ArrayList<String> sortedCounter = new ArrayList<String>(eventCounter.keySet());
            Collections.sort(sortedCounter);
            for (String key : sortedCounter) {
                System.out.println("For event: " + key + " count is " + eventCounter.get(key));
            }
        }
        System.out.println("Finished running at tick " + getPhase());

        System.out.println("With settings: " + Globals.getGlobalsString());
        System.out.println("L1Cache size is " + Integer.toString(Globals.L1_SIZE_IN_NUMBER_OF_LINES));
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
            isRunning = false;
        } catch (InterruptedException e) {
            return;
        }
    }

    private static class ComponentWrapper implements AsyncComponentInterface {

        CyclicBarrier prepBarrier, clockBarrier;
        public ComponentInterface component;

        public ComponentWrapper(ComponentInterface c) {
            this.component = c;
        }

        public String toString() {
            return "ComponentWrapper(" + component.toString() + ")";
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

    public static void runTicks(int numTicks) {
        synchronized (tickLock) {
            while (numTicks > 0) {

                final AtomicInteger counter = new AtomicInteger(0);
                final Thread[] workers = new Thread[components.size()];
                for (int i = 0; i < workers.length; i++) {
                    workers[i] = new Thread(new Runnable() {

                        public void run() {
                            ComponentInterface r = ((ComponentWrapper) components.get(counter.getAndIncrement())).component;
                            r.runPrep();

                        }
                    });
                    workers[i].start();
                }
                prepCount++;
                for (int i = 0; i < workers.length; i++) {
                    try {
                        workers[i].join();
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(Simulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
                clockCount++;
                final AtomicInteger newCounter = new AtomicInteger(0);
                for (int i = 0; i < workers.length; i++) {
                    workers[i] = new Thread(new Runnable() {

                        public void run() {
                            ComponentInterface r = ((ComponentWrapper) components.get(
                                    newCounter.getAndIncrement())).component;
                            r.runClock();

                        }
                    });
                    workers[i].start();
                }
                for (int i = 0; i < workers.length; i++) {
                    try {
                        workers[i].join();
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(Simulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
                numTicks--;
            }
        }
    }

    public static void runSimulation() {
        isRunning = true;
        final Thread[] workers = new Thread[components.size()];
        final CyclicBarrier prepStart = new CyclicBarrier(components.size() + 1,
                new Runnable() {

                    public void run() {
                        inPrep = true;
                    }
                });
        final CyclicBarrier clockStart = new CyclicBarrier(components.size() + 1,
                new Runnable() {

                    public void run() {
                        inPrep = false;
                    }
                });
        for (int i = 0; i < workers.length; i++) {
            AsyncComponentInterface r = components.get(i);
            r.configure(prepStart, clockStart);
            workers[i] = new Thread(r, r.toString() + "-" + Integer.toString(i));
            workers[i].start();
        }
        simulatorThread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (true) {
                        logger.debug("Starting prep " + Integer.toString(prepCount));
                        prepStart.await(3, TimeUnit.SECONDS);
                        clockCount += 1;
                        clockStart.await(3, TimeUnit.SECONDS);
                        prepCount += 1;
                    }
                } catch (InterruptedException ex) {
                    logger.info("recieved interrupt probably caused by stop call");
                } catch (BrokenBarrierException b) {
                    logger.warn("Simulator thread got BorkenBarrier exception, worker thread killed by someone else");
                } catch (TimeoutException t) {
                    logger.fatal("A simluator phase stalled for at least 3 seconds");
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
        }, "simulatorThread");
        simulatorThread.start();
    }
}
