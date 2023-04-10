package com.dissertation.validation;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

import io.grpc.netty.shaded.io.netty.util.internal.ThreadLocalRandom;

public abstract class LoadGenerator {
    protected ScheduledThreadPoolExecutor scheduler;

    protected final int regionPartitions;
    protected final int delay;
    protected final int clients;

    protected CountDownLatch startSignal;

    protected static final int MAX_THREADS = 20;
    protected static final int DELAY = 200;
    protected static final int CLIENTS = 3;

    public LoadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int operationsPerPartition, int clients) {
        this.scheduler = scheduler;

        this.regionPartitions = regionPartitions;
        this.delay = delay;
        this.clients = clients;

        this.startSignal = new CountDownLatch(1);
    }

    protected KeyPartition getRandomKey(Set<Integer> partitions) {
        int partitionId = -1;
        String key = "";
        do {
            key = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) + 'a'));
            partitionId = Utils.getKeyPartitionId(key);
        } while (!partitions.contains(partitionId));

        return new KeyPartition(key, partitionId);
    }

    protected class KeyPartition {
        private String key;
        private int partitionId;

        public KeyPartition(String key, int partitionId) {
            this.key = key;
            this.partitionId = partitionId;
        }

        public String getKey() {
            return this.key;
        }

        public int getPartitionId() {
            return this.partitionId;
        }
    }
}
