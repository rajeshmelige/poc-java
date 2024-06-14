package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CacheDebounceWithReentrantLock {

    public static Lock lock = new ReentrantLock();
    public static Map<Integer, String> cache = new HashMap<>();

    public static void main(String[] argss) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();

        Runnable task = () -> {
            getResource(1);
        };

        for(int i=1; i<=10; i++) {
            Thread thread = new Thread(task);
            threadList.add(thread);
            thread.start();
        }
        for(Thread t: threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Thread.sleep(1000);
        Thread lastThread = new Thread(task);

        lastThread.start();
        lastThread.join();
    }

    private static String getResource(int key) {
        System.out.println(Thread.currentThread().getName() + " " + "task started");
        if(cache.containsKey(key)) {
            System.out.println("Thread " +Thread.currentThread().getName() + " got value from cache " + cache.get(key));
            return cache.get(key);
        }
        String value = null;

        //lock.tryLock() - Non blocking lock. If the lock is not acquired, the thread skips the critical section and returns null
        // lock.lock is blocking lock. The thread is blocked until the lock is acquired
        lock.lock();
            try {
                if(cache.containsKey(key)) {
                    value = cache.get(key);
                    System.out.println("Thread " +Thread.currentThread().getName() + " got value from cache after lock " +value);
                } else {
                    value = getResourceFromDb(key);
                    cache.put(key, value);
                    System.out.println("Thread " +Thread.currentThread().getName() + " got value from db - " +value );
                }
            } finally {
                lock.unlock();
            }
        return value;
    }

    private static String getResourceFromDb(int key) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "db-x";
    }
}
