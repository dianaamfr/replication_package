package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class WriteGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private final int delay;
    private final int bytes;
    private final int writesPerPartition;
    private final int clientsPerPartition;
    private List<String> partitions;
    private CountDownLatch startSignal;
    private AtomicIntegerArray counters;
    private List<CountDownLatch> countDowns;

    private static final int MAX_THREADS = 20;
    private static final int OBJECT_BYTES = 8;
    private static final int WRITE_DELAY = 200;
    private static final int PARTITION_WRITES = 15;
    private static final int PARTITION_CLIENTS = 1;

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, int delay, int bytes,
            int writesPerPartition, int clientsPerPartition) {
        this.scheduler = scheduler;
        this.delay = delay;
        this.bytes = bytes;
        this.writesPerPartition = writesPerPartition;
        this.clientsPerPartition = clientsPerPartition;
        this.partitions = new ArrayList<>(Config.getPartitions());
        this.startSignal = new CountDownLatch(1);
        this.counters = new AtomicIntegerArray((new ArrayList<Integer>(Collections.nCopies(this.partitions.size(), 0)))
                .stream().mapToInt(i -> i).toArray());
        this.countDowns = new ArrayList<>();
        this.init();
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS);

        try {
            int delay = args.length > 0 ? Integer.parseInt(args[0]) : WRITE_DELAY;
            int bytes = args.length > 1 ? Integer.parseInt(args[1]) : OBJECT_BYTES;
            int writesPerPartition = args.length > 2 ? Integer.parseInt(args[2]) : PARTITION_WRITES;
            int clientsPerPartition = args.length > 3 ? Integer.parseInt(args[3]) : PARTITION_CLIENTS;
            WriteGenerator writeGenerator = new WriteGenerator(scheduler, delay, bytes,
                    writesPerPartition, clientsPerPartition);
            writeGenerator.run();
        } catch (NumberFormatException e) {
            System.err.println("Usage: WriteGenerator <delay:Int> <bytes:Int>");
        }
    }

    private void init() {
        for (int i = 0; i < this.partitions.size(); i++) {
            CountDownLatch writeSignal = new CountDownLatch(this.writesPerPartition);
            countDowns.add(writeSignal);
            for (int j = 0; j < this.clientsPerPartition; j++) {
                try {
                    Client c = new Client(getRandomRegion(this.partitions.get(i)));
                    this.scheduler.schedule(
                            new PartitionWriteGenerator(c, this.partitions.get(i), i, writeSignal),
                            0, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    System.err.println("Error: " + e.toString());
                }
            }
        }
    }

    public void run() {
        startSignal.countDown();
        for (int i = 0; i < this.partitions.size(); i++) {
            try {
                countDowns.get(i).await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.scheduler.shutdown();
    }

    private Region getRandomRegion(String partition) {
        List<Region> regions = new ArrayList<>(Config.getPartitionRegions(partition));
        return regions.get(ThreadLocalRandom.current().nextInt(regions.size()));
    }

    private String getRandomKey(String partition) {
        String result = "";
        int partitionId = Integer.valueOf(partition.split("partition")[1]);
        do {
            result = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) + 'a'));
        } while ((Math.floorMod(result.hashCode(), this.partitions.size()) + 1) != partitionId);
        return result;
    }

    private class PartitionWriteGenerator implements Runnable {
        private Client client;
        private String partition;
        private int index;
        private CountDownLatch writeSignal;

        public PartitionWriteGenerator(Client client, String partition, int index, CountDownLatch writeSignal) {
            this.client = client;
            this.partition = partition;
            this.index = index;
            this.writeSignal = writeSignal;
        }

        @Override
        public void run() {
            String key = getRandomKey(this.partition);
            byte[] value = Utils.getRandomByteArray(bytes);
            try {
                startSignal.await();
                if (counters.getAndIncrement(index) < writesPerPartition) {
                    this.client.requestWrite(key, value);
                    scheduler.schedule(
                            new PartitionWriteGenerator(this.client, this.partition, this.index, this.writeSignal),
                            delay, TimeUnit.MILLISECONDS);
                    this.writeSignal.countDown();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
