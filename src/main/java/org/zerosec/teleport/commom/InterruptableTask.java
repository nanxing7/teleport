package org.zerosec.teleport.commom;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:sanyuedev@gmail.com">ssyo.top</a>
 * created at 2021-11-18 10:06
 */
public class InterruptableTask implements Runnable {
    private AtomicInteger count = new AtomicInteger();
    private Lock lock = new ReentrantLock();
    private volatile boolean suspended = false;

    public void suspend() {
        lock.lock();
        suspended = true;
        lock.unlock();
    }

    public void resume() {
        lock.lock();
        suspended = false;
        lock.unlock();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            lock.lock();
            try {
                if (!suspended) {
                    //Do work here
                    System.out.println("运行次数：" + count.incrementAndGet());
                } else {
                    //Has been suspended
                    try {
                        while (suspended) {
                            lock.wait();
                            lock.notifyAll();
                        }
                    } catch (InterruptedException e) {
                        //
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        System.out.println("Cancelled");
    }
}
