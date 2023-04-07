package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

public class ReadGenerator {
    private ScheduledThreadPoolExecutor scheduler;

    private final int regionPartitions;
    private final int delay;
    private final int readsPerPartition;
    private final int clients;
    private CountDownLatch startSignal;
    private AtomicIntegerArray counters;
    private ConcurrentMap<Integer, CountDownLatch> countDowns;
    private ConcurrentMap<Integer, Integer> partitionCounterPos;

    private static final int MAX_THREADS = 20;
    private static final int READ_DELAY = 200;
    private static final int READS_PER_PARTITION = 15;
    private static final int MAX_KEYS_PER_READ = 2;
    private static final int CLIENTS = 3;
    private static final String USAGE = "Usage: ClientInterface <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <readsPerPartition:Int> <clients:Int>";

    public ReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int readsPerPartition, int clients) {
        this.scheduler = scheduler;

        this.regionPartitions = regionPartitions;
        this.delay = delay;
        this.readsPerPartition = readsPerPartition;
        this.clients = clients;

        this.startSignal = new CountDownLatch(1);
        this.counters = new AtomicIntegerArray(
                Collections.nCopies(this.regionPartitions, 0).stream().mapToInt(i -> i).toArray());
        this.partitionCounterPos = new ConcurrentHashMap<>();
        this.countDowns = new ConcurrentHashMap<>();
        this.init(readAddress, writeAddresses);
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS);
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 6) {
            System.err.println(USAGE);
            return;
        }

        try {
            regionPartitions = Integer.parseInt(args[0]);
            readAddress = new Address(Integer.parseInt(args[1]), args[2]);
            for (int i = 3; i < regionPartitions; i += 3) {
                writeAddresses.add(new Address(Integer.parseInt(args[i]), args[i + 1], Integer.parseInt(args[i + 2])));
            }

            int delay = args.length > regionPartitions + 3 ? Integer.parseInt(args[regionPartitions + 3]) : READ_DELAY;
            int readsPerPartition = args.length > regionPartitions + 4 ? Integer.parseInt(args[regionPartitions + 4])
                    : READS_PER_PARTITION;
            int clients = args.length > regionPartitions + 5 ? Integer.parseInt(args[regionPartitions + 5]) : CLIENTS;

            ReadGenerator readGenerator = new ReadGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, readsPerPartition, clients);
            readGenerator.run();
        } catch (Exception e) {
            System.err.println(USAGE);
        }
    }

    private void init(Address readAddress, List<Address> writeAddresses) {
        // Init countdowns to wait until all partitions handle the same number of reads
        for (int i = 0; i < regionPartitions; i++) {
            CountDownLatch readSignal = new CountDownLatch(this.readsPerPartition);
            this.countDowns.put(i, readSignal);
            this.partitionCounterPos.put(writeAddresses.get(i).getPartitionId(), i);
        }

        // Init clients
        for (int j = 0; j < this.clients; j++) {
            try {
                Client c = new Client(readAddress, writeAddresses);
                this.scheduler.schedule(
                        new RegionReadGenerator(c),
                        0, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
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

    private class RegionReadGenerator implements Runnable {
        private Client client;

        public RegionReadGenerator(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            ConcurrentMap<Integer, Set<String>> keys = getRandomKeys();
            try {
                startSignal.await();
                for(Entry<Integer, Set<String>> entry: keys.entrySet()) {
                    int counterPos = partitionCounterPos.get(entry.getKey());
                    if (counters.getAndIncrement(counterPos) < readsPerPartition) {
                        this.client.requestROT(entry.getValue());
                        scheduler.schedule(
                                new RegionReadGenerator(this.client),
                                delay, TimeUnit.MILLISECONDS);
                        countDowns.compute(counterPos, (key, countDown) -> {
                            countDown.countDown();
                            return countDown;
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private ConcurrentMap<Integer, Set<String>> getRandomKeys() {
        ConcurrentMap<Integer, Set<String>> keys = new ConcurrentHashMap<>();
        int keysPerRead = ThreadLocalRandom.current().nextInt(1, MAX_KEYS_PER_READ + 1);
        while(keys.size() < keysPerRead) {
            KeyPartition keyPartition = getRandomKey(); 
            if(!keys.containsKey(keyPartition.getPartitionId())) {
                keys.put(keyPartition.getPartitionId(), new HashSet<>());
            }
            keys.get(keyPartition.getPartitionId()).add(keyPartition.getKey());
        }
        return keys;
    }

    public KeyPartition getRandomKey() {
        int partitionId = -1;
        String key = "";
        do {
            key = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) + 'a'));
            partitionId = Utils.getKeyPartitionId(key);
        } while (!this.countDowns.keySet().contains(partitionId));
        
        return new KeyPartition(key, partitionId);
    } 

    private class KeyPartition {
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
