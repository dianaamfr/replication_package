package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

import io.grpc.netty.shaded.io.netty.util.internal.ThreadLocalRandom;

public abstract class LoadGenerator {
    protected ScheduledThreadPoolExecutor scheduler;

    protected final int regionPartitions;
    protected final int delay;
    protected final int clients;
    protected final int operationsPerPartition;
    
    protected CountDownLatch startSignal;
    protected AtomicIntegerArray counters;
    protected ConcurrentMap<Integer, Integer> partitionIndexes;
    protected AtomicReferenceArray<CountDownLatch> countDowns;

    protected static final int MAX_THREADS = 20;
    protected static final int DELAY = 200;
    protected static final int CLIENTS = 3;


    public LoadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses, int regionPartitions, int delay, int operationsPerPartition, int clients) {
        this.scheduler = scheduler;

        this.regionPartitions = regionPartitions;
        this.delay = delay;
        this.operationsPerPartition = operationsPerPartition;
        this.clients = clients;

        this.startSignal = new CountDownLatch(1);
        this.partitionIndexes = new ConcurrentHashMap<>();
        this.counters = new AtomicIntegerArray((new ArrayList<Integer>(Collections.nCopies(this.regionPartitions, 0)))
                .stream().mapToInt(i -> i).toArray());
        this.countDowns = new AtomicReferenceArray<>(this.regionPartitions);
    }

    protected void init(Address readAddress, List<Address> writeAddresses) {
        // Init countdowns to wait until all partitions handle the same number of operations
        for (int i = 0; i < this.regionPartitions; i++) {
            this.countDowns.set(i, new CountDownLatch(this.operationsPerPartition)); 
            this.partitionIndexes.put(writeAddresses.get(i).getPartitionId(), i);
        }
    }

    public void run() {
        startSignal.countDown();
        for (int i = 0; i < this.regionPartitions; i++) {
            try {
                this.countDowns.get(i).await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.scheduler.shutdown();
    }

    protected KeyPartition getRandomKey() {
        int partitionId = -1;
        String key = "";
        do {
            key = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) + 'a'));
            partitionId = Utils.getKeyPartitionId(key);
        } while (!this.partitionIndexes.keySet().contains(partitionId));
        
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
