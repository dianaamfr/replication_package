package com.dissertation.validation;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import com.dissertation.utils.Address;

public abstract class LoadGenerator {
    protected ScheduledThreadPoolExecutor scheduler;

    protected final int regionPartitions;
    protected final long delay;

    protected CountDownLatch startSignal;
    protected final Set<Integer> partitions;

    protected static final int MAX_THREADS = 20;
    protected static final long DELAY = 500;

    public LoadGenerator(ScheduledThreadPoolExecutor scheduler, List<Address> writeAddresses, int regionPartitions, long delay) {
        this.scheduler = scheduler;

        this.regionPartitions = regionPartitions;
        this.delay = delay;
        
        this.partitions = writeAddresses.stream()
        .map(address -> address.getPartitionId()).collect(Collectors.toUnmodifiableSet());
        this.startSignal = new CountDownLatch(1);
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
