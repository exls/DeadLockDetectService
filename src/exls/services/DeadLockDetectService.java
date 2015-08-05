package exls.services;

import java.io.PrintStream;
import java.lang.management.*;
import java.util.HashMap;

/**
 * DeadLock detect service
 *
 * @author Anton Pavlov <anton.pavlov.it@gmail.com>
 * @example <code>
 * (new DeadLockDetectService()).start();
 * </code>
 */
public class DeadLockDetectService extends Thread {
    /**
     * Min sleep time milliseconds
     */
    private static final int MIN_SLEEP_TIME = 100;
    /**
     * Sleep time milliseconds
     */
    private static int sleepTime = 2000;
    /**
     * Print stream
     *
     * @default System.err
     */
    private static PrintStream printStream = System.err;

    /**
     * Setter of sleep time milliseconds
     *
     * @param sleepTime
     */
    public static void setSleepTime(int sleepTime) {
        //check min sleep time
        if (sleepTime < MIN_SLEEP_TIME) {
            throw new IllegalArgumentException("Sleep time must be more than " + MIN_SLEEP_TIME);
        }
        DeadLockDetectService.sleepTime = sleepTime;
    }

    /**
     * Setter of print stream
     *
     * @param printStream
     */
    public static void setPrintStream(PrintStream printStream) {
        DeadLockDetectService.printStream = printStream;
    }

    /**
     * Deadlock detected
     */
    private static boolean isDetected = false;

    /**
     * Constructor
     */
    public DeadLockDetectService() {
        //set as daemon
        setDaemon(true);
    }

    /**
     * Check deadlock
     */
    private void checkDeadLock() {
        //Get all threads and get info for all
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] ti = bean.getThreadInfo(bean.getAllThreadIds(), true, true);
        //
        HashMap<Integer, Integer> lockedMap = new HashMap<Integer, Integer>();
        //check lock info an each thread
        for (int i = 0; i < ti.length; i++) {
            LockInfo lockInfo = ti[i].getLockInfo();
            if (lockInfo != null) {
                // get blocked identity hash code
                int lockHash = lockInfo.getIdentityHashCode();
                //check in all stacktraces locked objects through monitors
                StackTraceElement[] stackTrace = ti[i].getStackTrace();
                MonitorInfo[] monitors = ti[i].getLockedMonitors();
                for (int j = 0; j < stackTrace.length && j < 8; j++) {
                    for (MonitorInfo mi : monitors) {
                        if (mi.getLockedStackDepth() == j) {
                            int lockedHash = mi.getIdentityHashCode();
                            //check cintains lockedHash in map and dispatch message to print stream
                            if (lockedMap.containsKey(lockHash) && lockedMap.get(lockHash).intValue() == lockedHash) {
                                //don't dispatch again if before detected
                                if (!isDetected) {
                                    printStream.println("Deadlock found");
                                    isDetected ^= true;
                                }
                                return;
                            }
                            //put hashes to map
                            lockedMap.put(lockedHash, lockHash);
                        }
                    }
                }
            }
        }
        isDetected = false;
    }

    @Override
    public void run() {
        //every next sleep time milliseconds check deadlock
        while (true) {
            try {
                sleep(sleepTime);
            } catch (InterruptedException e) {
                //immediately check deadlock if interrupted
            }
            checkDeadLock();
        }
    }
}
