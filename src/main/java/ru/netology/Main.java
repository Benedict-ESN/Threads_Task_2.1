package ru.netology;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int numThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();
        Thread loggerThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                        printCurrentLeader();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        loggerThread.start();

        for (int i = 0; i < numThreads; i++) {
            futures.add(executor.submit(() -> {
                String route = generateRoute("RLRFR", 100);
                int rCount = countR(route);
                synchronized (sizeToFreq) {
                    sizeToFreq.put(rCount, sizeToFreq.getOrDefault(rCount, 0) + 1);
                    sizeToFreq.notify();
                }
                System.out.println("Route: " + route + " -> R count: " + rCount);
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        loggerThread.interrupt();
        printResults();
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static int countR(String route) {
        int count = 0;
        for (char c : route.toCharArray()) {
            if (c == 'R') {
                count++;
            }
        }
        return count;
    }

    public static Map.Entry<Integer, Integer> countLeader() {
        synchronized (sizeToFreq) {
            if (sizeToFreq.isEmpty()) return null;
            return Collections.max(sizeToFreq.entrySet(), Map.Entry.comparingByValue());
        }
    }


    public static void printCurrentLeader() {
        synchronized (sizeToFreq) {
            if (sizeToFreq.isEmpty()) return;
            Map.Entry<Integer, Integer> leader = countLeader();
            if (leader != null) {
                System.out.println("Текущий лидер по повторениям - " + leader.getKey() + " (встретился " + leader.getValue() + " раз)");
            }
        }
    }

    public static void printResults() {
        synchronized (sizeToFreq) {
            Map.Entry<Integer, Integer> leader = countLeader();
            if (leader != null) {
                int maxCount = leader.getKey();
                int maxFrequency = leader.getValue();

                System.out.println("Самое частое количество повторений - " + maxCount + " (встретилось " + maxFrequency + " раз)");
                System.out.println("Другие размеры:");

                sizeToFreq.entrySet().stream()
                        .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                        .forEach(entry -> {
                            if (entry.getKey() != maxCount) {
                                System.out.println("- " + entry.getKey() + " (" + entry.getValue() + " раз)");
                            }
                        });
            }
        }
    }
}