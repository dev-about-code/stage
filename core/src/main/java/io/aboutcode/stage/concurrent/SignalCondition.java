package io.aboutcode.stage.concurrent;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * <p>A waiting condition that allows interested parties to be notified on a signal triggered by
 * another party, even after signalling has already happened. This is commonly used to signal
 * finished execution in a multi-threaded environment.</p>
 * <p>See {@link Condition} for more information on the implemented methods.</p>
 */
public class SignalCondition implements Condition {
    private final Object monitor = new Object();
    private volatile boolean signalled;

    /**
     * @see Condition#await()
     */
    @Override
    public void await() throws InterruptedException {
        awaitInternal();
    }

    /**
     * @see Condition#awaitUninterruptibly()
     */
    @Override
    public void awaitUninterruptibly() {
        do {
            try {
                awaitInternal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        while (!signalled);
    }

    /**
     * Always returns 0.
     *
     * @see Condition#awaitNanos(long)
     */
    @Override
    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        awaitInternal(0, nanosTimeout);
        return 0;
    }

    /**
     * @see Condition#await(long, TimeUnit)
     */
    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        long millis = unit.toMillis(time);
        long nanos = unit.toNanos(time) - TimeUnit.MILLISECONDS.toNanos(millis);
        return awaitInternal(millis, nanos);
    }

    /**
     * @see Condition#awaitUntil(Date)
     */
    @Override
    public boolean awaitUntil(Date deadline)
            throws InterruptedException {
        return awaitInternal(System.currentTimeMillis() - deadline.getTime(), 0);
    }

    private boolean awaitInternal(long millisecods, long nanos) throws InterruptedException {
        long millis = millisecods;
        if (signalled) {
            return true;
        }

        if (millis < Long.MAX_VALUE && (nanos > 500000 || nanos > 0 && millis == 0)) {
            millis++;
        }

        if (millis == 0) {
            return awaitInternal();
        }

        long timeout = System.nanoTime() + millis * 1000000;
        long remainingMillis = millis;
        synchronized (monitor) {
            while (!signalled && remainingMillis > 0) {
                monitor.wait(remainingMillis);
                long remainingNanos = timeout - System.nanoTime();
                remainingMillis = remainingNanos / 1000000;
                if (remainingNanos % 1000000 > 500000) {
                    remainingMillis++;
                }
            }
        }

        return signalled;
    }

    /**
     * Returns whether this condition has been signalled already.
     *
     * @return True if the condition has been signalled, false otherwise.
     */
    public boolean isSignalled() {
        return signalled;
    }

    private boolean awaitInternal() throws InterruptedException {
        synchronized (monitor) {
            while (!signalled) {
                monitor.wait();
            }
        }
        return signalled;
    }

    /**
     * @see Condition#signal()
     */
    @Override
    public void signal() {
        synchronized (monitor) {
            signalled = true;
            monitor.notify();
        }
    }

    /**
     * @see Condition#signalAll()
     */
    @Override
    public void signalAll() {
        synchronized (monitor) {
            signalled = true;
            monitor.notifyAll();
        }
    }
}
