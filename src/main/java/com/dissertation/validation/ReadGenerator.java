package com.dissertation.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;

public class ReadGenerator extends LoadGenerator {
    private final int readsPerPartition;

    private static final int READS_PER_PARTITION = 15;
    private static final int MAX_KEYS_PER_READ = 2;

    private static final String USAGE = "Usage: ReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <readsPerPartition:Int>";

    public ReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int readsPerPartition, int clients) {
        super(scheduler, readAddress, writeAddresses, regionPartitions, delay, readsPerPartition, clients);
        this.readsPerPartition = readsPerPartition;
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
            int readsPerPartition = args.length > addressesEndIndex + 1 ? Integer.parseInt(args[addressesEndIndex + 1])
                    : READS_PER_PARTITION;
            int clients = args.length > addressesEndIndex + 2 ? Integer.parseInt(args[regionPartitions + 2]) : CLIENTS;

            ReadGenerator readGenerator = new ReadGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, readsPerPartition, clients);
            readGenerator.run();
        } catch (Exception e) {
            System.err.println(USAGE);
        }
    }

    @Override
    protected void init(Address readAddress, List<Address> writeAddresses) {
        super.init(readAddress, writeAddresses);

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

    private class ReadGeneratorRequest implements Runnable {
        private Client client;

        public ReadGeneratorRequest(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            ConcurrentMap<Integer, Set<String>> keys = getRandomKeys();
            try {
                startSignal.await();
                for (Entry<Integer, Set<String>> entry : keys.entrySet()) {
                    int counterPos = partitionIndexes.get(entry.getKey());
                    if (counters.getAndIncrement(counterPos) < readsPerPartition) {
                        this.client.requestROT(entry.getValue());
                        scheduler.schedule(
                                new ReadGeneratorRequest(this.client), delay, TimeUnit.MILLISECONDS);
                        countDowns.updateAndGet(counterPos, (countDown) -> {
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
        while (keys.size() < keysPerRead) {
            KeyPartition keyPartition = this.getRandomKey();
            if (!keys.containsKey(keyPartition.getPartitionId())) {
                keys.put(keyPartition.getPartitionId(), new HashSet<>());
            }
            keys.get(keyPartition.getPartitionId()).add(keyPartition.getKey());
        }
        return keys;
    }
}
