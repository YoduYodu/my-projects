package hotel;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom reentrant read/write lock that allows:
 * 1) Multiple readers (when there is no writer). Any thread can acquire multiple read locks (if nobody is writing).
 * 2) One writer (when nobody else is writing or reading).
 * 3) A writer is allowed to acquire a read lock while holding the write lock.
 * 4) A writer is allowed to acquire another write lock while holding the write lock.
 * 5) A reader can not acquire a write lock while holding a read lock.
 *
 * Use ReentrantReadWriteLockTest to test this class.
 * The code is modified from the code of Prof. Rollins.
 */
public class ReentrantReadWriteLock {
    private Map<Long, Integer> readerCounter;
    private Map<Long, Integer> writerCounter;

    /**
     * Constructor for ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock() {
        readerCounter = new HashMap<>();
        writerCounter = new HashMap<>();
    }

    /**
     * Return true if the current thread holds a read lock.
     *
     * @return true or false
     */
    public synchronized boolean isReadLockHeldByCurrentThread() {
        return readerCounter.containsKey(Thread.currentThread().getId());
    }

    /**
     * Return true if the current thread holds a write lock.
     *
     * @return true or false
     */
    public synchronized boolean isWriteLockHeldByCurrentThread() {
        return writerCounter.containsKey(Thread.currentThread().getId());
    }

    /**
     * Non-blocking method that attempts to acquire the read lock. Returns true
     * if successful.
     * Checks conditions (whether it can acquire the read lock), and if they are true,
     * updates readers info.
     *
     * Note that if conditions are false (can not acquire the read lock at the moment), this method
     * does NOT wait, just returns false
     * @return true if the read lock is acquired
     */
    public synchronized boolean tryAcquiringReadLock() {
        Long threadId = Thread.currentThread().getId();
        if (writerCounter.size() != 0 && !isWriteLockHeldByCurrentThread()) {
            return false;
        } else {
            readerCounter.put(threadId, readerCounter.getOrDefault(threadId, 0) + 1);
            return true;
        }
    }

    /**
     * Non-blocking method that attempts to acquire the write lock. Returns true
     * if successful.
     * Checks conditions (whether it can acquire the write lock), and if they are true,
     * updates writers info.
     *
     * Note that if conditions are false (can not acquire the write lock at the moment), this method
     * does NOT wait, just returns false
     *
     * @return true if the writer lock is acquired
     */
    public synchronized boolean tryAcquiringWriteLock() {
        Long threadId = Thread.currentThread().getId();
        if (readerCounter.size() != 0) {
            return false;
        } else if (writerCounter.size() > 0 && !isWriteLockHeldByCurrentThread()) {
            return false;
        } else {
            writerCounter.put(threadId, writerCounter.getOrDefault(threadId, 0) + 1);
            return true;
        }
    }

    /**
     * Blocking method that will return only when the read lock has been
     * acquired.
     * Calls tryAcquiringReadLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     *
     */
    public synchronized void lockRead() {
        while (!tryAcquiringReadLock()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * Releases the read lock held by the calling thread. Other threads might
     * still be holding read locks. If no more readers after unlocking, calls notifyAll().
     */
    public synchronized void unlockRead() {
        Long threadId = Thread.currentThread().getId();
        if (isReadLockHeldByCurrentThread()) {
            readerCounter.put(threadId, readerCounter.get(threadId) - 1);
        }

        // Remove the entry if a reader contains 0 lock
        if (readerCounter.get(threadId) == 0) {
            readerCounter.remove(threadId);
        }

        // Calls notifyAll if no readers
        if (readerCounter.size() == 0) {
            notifyAll();
        }
    }

    /**
     * Blocking method that will return only when the write lock has been
     * acquired.
     * Calls tryAcquiringWriteLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     */
    public synchronized void lockWrite() {
        while (!tryAcquiringWriteLock()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * Releases the write lock held by the calling thread. The calling thread
     * may continue to hold a read lock.
     * If the number of writers becomes 0, calls notifyAll.
     */
    public synchronized void unlockWrite() {
        Long threadId = Thread.currentThread().getId();
        if (isWriteLockHeldByCurrentThread()) {
            writerCounter.put(threadId, writerCounter.get(threadId) - 1);
        }

        // Remove the entry if a writer contains 0 lock
        if (writerCounter.get(threadId) == 0) {
            writerCounter.remove(threadId);
        }

        // Calls notifyAll if no writers
        if (writerCounter.size() == 0) {
            notifyAll();
        }
    }
}
