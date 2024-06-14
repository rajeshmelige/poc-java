package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class CacheDebounceWithSemaphore {

    public static Map<Integer, String> cache = new HashMap<>();
    public static final Map<Integer, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<>();
        for(int i=1; i<=10; i++) {
            Thread thread = new Thread(() -> {
                getResource(1);
            });
            thread.start();
            threads.add(thread);
        }

        for(int i=0; i<10; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Thread.sleep(1000);
        Thread finalThread = new Thread(() -> {
            getResource(1);
        });

        finalThread.start();
        finalThread.join();
    }

    private static String getResource(int key) {
        System.out.println(Thread.currentThread().getName() + " " + "task started");
        String value;
        if(cache.containsKey(key)) {
            System.out.println("Thread " +Thread.currentThread().getName() + " got value from cache");
            return cache.get(key);
        }
        Semaphore semaphore = semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        try {
            semaphore.acquire();
            System.out.println("Thread " +Thread.currentThread().getName() + " acquired semaphore");
            if(cache.containsKey(key)) {
                value = cache.get(key);
                System.out.println("Thread " +Thread.currentThread().getName() + " got value from cache after being locked");
            } else {
                value = getResourceFromDB(key);
                cache.put(key, value);
                System.out.println("Thread " +Thread.currentThread().getName() + " got value from db");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }

        semaphoreMap.remove(key);
        System.out.println("removed value from map");
        return value;
    }

    private static String getResourceFromDB(int i) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "x";
    }
}