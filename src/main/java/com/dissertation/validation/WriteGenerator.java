package com.dissertation.validation;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.referencearchitecture.config.Config;
import com.dissertation.referencearchitecture.exceptions.InvalidRegionException;
import com.dissertation.utils.Utils;

public class WriteGenerator {
    private ScheduledThreadPoolExecutor scheduler;
    private static final int MAX_THREADS = 4;
    private static final int OBJECT_BYTES = 8;
    private static final int DELAY = 1000;
    private Random random;
    private int numPartitions;

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, Random random) {
        this.scheduler = scheduler;
        this.random = random;
        initWriteThreads();
        getRandomKey("partition1");
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS);
        new WriteGenerator(scheduler, new Random(1));
    }

    private void initWriteThreads() {
        Set<String> partitions = Config.getPartitions();
        this.numPartitions = partitions.size();

        for(String partition: partitions) {
            Client c;
            try {
                c = new Client(getRandomRegion(partition));
                this.scheduler.scheduleWithFixedDelay(new PartitionWriteGenerator(c, partition), 0, DELAY, TimeUnit.MILLISECONDS);
            } catch (RemoteException | InvalidRegionException |NotBoundException e) {
                System.err.println("Error: " + e.toString());
            }
        }
    }

    private String getRandomRegion(String partition) {
        List<String> regions = new ArrayList<>(Config.getPartitionRegions(partition));
        return regions.get(this.random.nextInt(regions.size()));
    }

    private String getRandomKey(String partition) {
        String result = "";
        int partitionId = Integer.valueOf(partition.split("partition")[1]);
        do {
            result = String.valueOf((char)(this.random.nextInt(26) + 'a'));
        } while((Math.floorMod(result.hashCode(), this.numPartitions) + 1) != partitionId);
        return result;
    }

    private class PartitionWriteGenerator implements Runnable {
        Client client;
        private String partition;

        public PartitionWriteGenerator(Client client, String partition) {
            this.client = client;
            this.partition = partition;
        }

        @Override
        public void run() {
            String key = getRandomKey(this.partition);
            byte[] value = Utils.getRandomByteArray(OBJECT_BYTES);
            this.client.requestWrite(key, value);
        }
    }
}
