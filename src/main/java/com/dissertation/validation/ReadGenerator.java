package com.dissertation.validation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;

public class ReadGenerator extends LoadGenerator {
    private final int totalReads;

    private static final int TOTAL_READS = 100;
    private static final int MAX_KEYS_PER_READ = 2;

    private AtomicInteger counter;
    private CountDownLatch countDown;
    private final Set<Integer> partitions;

    private static final String USAGE = "Usage: ReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <totalReads:Int>";

    public ReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int totalReads, int clients) {
        super(scheduler, regionPartitions, delay, clients);
        this.totalReads = totalReads;
        
        this.counter = new AtomicInteger(0);
        this.countDown = new CountDownLatch(this.totalReads);
        this.partitions = writeAddresses.stream()
            .map(address -> address.getPartitionId()).collect(Collectors.toUnmodifiableSet());
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
            int addressesEndIndex = regionPartitions * 3 + 3;
            for (int i = 3; i < addressesEndIndex; i += 3) {
                writeAddresses.add(new Address(Integer.parseInt(args[i]), args[i + 1], Integer.parseInt(args[i + 2])));
            }

            int delay = args.length > addressesEndIndex ? Integer.parseInt(args[addressesEndIndex]) : DELAY;
            int totalReads = args.length > addressesEndIndex + 1 ? Integer.parseInt(args[addressesEndIndex + 1])
                    : TOTAL_READS;
            int clients = args.length > addressesEndIndex + 2 ? Integer.parseInt(args[regionPartitions + 2]) : CLIENTS;

            ReadGenerator readGenerator = new ReadGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, totalReads, clients);
            readGenerator.run();
        } catch (Exception e) {
            System.err.println(USAGE);
        }
    }

    private void init(Address readAddress, List<Address> writeAddresses) {
        // Init clients
        for (int j = 0; j < this.clients; j++) {
            try {
                Client c = new Client(readAddress, writeAddresses);
                this.scheduler.schedule(
                        new ReadGeneratorRequest(c), 0, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public void run() {
        this.startSignal.countDown();
        try {
            this.countDown.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.scheduler.shutdown();
    }

    private class ReadGeneratorRequest implements Runnable {
        private Client client;

        public ReadGeneratorRequest(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            Set<String> keys = getRandomKeys();
            try {
                startSignal.await();
                if (counter.getAndIncrement() < totalReads) {
                    Instant start = Instant.now();
                    this.client.requestROT(keys);
                    Instant end = Instant.now();
                    System.out.println(Duration.between(start, end).toMillis());
                    scheduler.schedule(
                            new ReadGeneratorRequest(this.client), delay, TimeUnit.MILLISECONDS);
                    countDown.countDown();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private Set<String> getRandomKeys() {
        Set<String> keys = new HashSet<>();
        int keysPerRead = ThreadLocalRandom.current().nextInt(1, MAX_KEYS_PER_READ + 1);
        while (keys.size() < keysPerRead) {
            KeyPartition keyPartition = this.getRandomKey(this.partitions);
            keys.add(keyPartition.getKey());
        }
        return keys;
    }
}
