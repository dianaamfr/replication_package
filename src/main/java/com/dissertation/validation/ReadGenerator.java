package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.referencearchitecture.config.Config;

public class ReadGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private Random random;
    private final int delay;
    private final int keys;
    private final int readsPerRegion;
    private final int clientsPerRegion;
    private final List<String> regions;
    private final int numPartitions;
    private CountDownLatch startSignal;
    private AtomicIntegerArray counters;
    private List<CountDownLatch> countDowns;

    private static final int MAX_THREADS = 20;
    private static final int READ_KEYS = 3;
    private static final int READ_DELAY = 200;
    private static final int REGION_READS = 15;
    private static final int REGION_CLIENTS = 3;

    public ReadGenerator(ScheduledThreadPoolExecutor scheduler, Random random, int delay, int keys,
    int readsPerRegion, int clientsPerRegion) {
        this.scheduler = scheduler;
        this.random = random;
        this.delay = delay;
        this.keys = keys;
        this.readsPerRegion = readsPerRegion;
        this.clientsPerRegion = clientsPerRegion;
        this.regions = Config.getRegions().stream().collect(Collectors.toUnmodifiableList());
        this.numPartitions = Config.getPartitions().size();
        this.startSignal = new CountDownLatch(1);
        this.counters = new AtomicIntegerArray(Collections.nCopies(this.regions.size(), 0).stream().mapToInt(i -> i).toArray());
        this.countDowns = new ArrayList<>();
        this.init();
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS);
        try {
            int delay = args.length > 0 ? Integer.parseInt(args[0]) : READ_DELAY;
            int bytes = args.length > 1 ? Integer.parseInt(args[1]) : READ_KEYS;
            int readsPerRegion = args.length > 2 ? Integer.parseInt(args[2]) : REGION_READS;
            int clientsPerRegion = args.length > 3 ? Integer.parseInt(args[3]) : REGION_CLIENTS;
            ReadGenerator readGenerator = new ReadGenerator(scheduler, new Random(1), delay, bytes,
                    readsPerRegion, clientsPerRegion);
            readGenerator.run();
        } catch (NumberFormatException e) {
            System.err.println("Usage: WriteGenerator <delay:Int> <bytes:Int>");
        }
    }

    private void init() {
        for (int i = 0; i < this.regions.size(); i++) {
            CountDownLatch readSignal = new CountDownLatch(this.readsPerRegion);
            countDowns.add(readSignal);
            for (int j = 0; j < this.clientsPerRegion; j++) {
                try {
                    Client c = new Client(this.regions.get(i));
                    this.scheduler.schedule(
                            new RegionReadGenerator(c, this.regions.get(i), i, readSignal),
                            0, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    System.err.println("Error: " + e.toString());
                }
            }
        }
    }

    public void run() {
        startSignal.countDown();
        for (int i = 0; i < this.regions.size(); i++) {
            try {
                countDowns.get(i).await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.scheduler.shutdown();
    }

    private Set<String> getRandomKeys(String region) {
        Set<String> result = new HashSet<>();
        for(int i = 0; i < this.keys; i++) {
            String partition = getRandomPartition(region);
            int partitionId = Integer.valueOf(partition.split("partition")[1]);
            String aux = "";
            do {
                aux = String.valueOf((char) (this.random.nextInt(26) + 'a'));
            } while ((Math.floorMod(aux.hashCode(), this.numPartitions) + 1) != partitionId);
            result.add(aux);
        }
        return result;
    }

    private String getRandomPartition(String region) {
        List<String> partitions = new ArrayList<>(Config.getPartitions(region));
        return partitions.get(this.random.nextInt(partitions.size()));
    }


    private class RegionReadGenerator implements Runnable {
        private Client client;
        private String region;
        private int index;
        private CountDownLatch readSignal;

        public RegionReadGenerator(Client client, String region, int index, CountDownLatch readSignal) {
            this.client = client;
            this.region = region;
            this.index = index;
            this.readSignal = readSignal;
        }

        @Override
        public void run() {
            Set<String> keys = getRandomKeys(this.region);
            try {
                startSignal.await();
                if (counters.getAndIncrement(index) < readsPerRegion) {
                    this.client.requestROT(keys);
                    scheduler.schedule(
                            new RegionReadGenerator(this.client, this.region, this.index, this.readSignal),
                            delay, TimeUnit.MILLISECONDS);
                    this.readSignal.countDown();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
