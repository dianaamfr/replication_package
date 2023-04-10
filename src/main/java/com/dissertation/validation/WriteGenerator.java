package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class WriteGenerator extends LoadGenerator {
    private final int bytes;
    private final int writesPerPartition;

    private static final int OBJECT_BYTES = 8;
    private static final int WRITES_PER_PARTITION = 15;

    private AtomicIntegerArray counters;
    private ConcurrentMap<Integer, Integer> partitionIndexes;
    private AtomicReferenceArray<CountDownLatch> countDowns;

    private static final String USAGE = "Usage: WriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <clients:Int> <writesPerPartition:Int> <bytes:Int> ";

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int clients, int writesPerPartition, int bytes) {
        super(scheduler, readAddress, writeAddresses, regionPartitions, delay, writesPerPartition, clients);
        this.writesPerPartition = writesPerPartition;
        this.bytes = bytes;

        this.partitionIndexes = new ConcurrentHashMap<>();
        this.counters = new AtomicIntegerArray((new ArrayList<Integer>(Collections.nCopies(this.regionPartitions, 0)))
                .stream().mapToInt(i -> i).toArray());
        this.countDowns = new AtomicReferenceArray<>(this.regionPartitions);

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
            int bytes = args.length > addressesEndIndex + 1 ? Integer.parseInt(args[addressesEndIndex + 1])
                    : OBJECT_BYTES;
            int writesPerPartition = args.length > addressesEndIndex + 2 ? Integer.parseInt(args[addressesEndIndex + 2])
                    : WRITES_PER_PARTITION;
            int clients = args.length > addressesEndIndex + 3 ? Integer.parseInt(args[addressesEndIndex + 3]) : CLIENTS;
            WriteGenerator writeGenerator = new WriteGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, bytes, writesPerPartition, clients);
            writeGenerator.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }


    private void init(Address readAddress, List<Address> writeAddresses) {
        // Init countdowns to wait until all partitions handle the same number of
        // operations
        for (int i = 0; i < this.regionPartitions; i++) {
            this.countDowns.set(i, new CountDownLatch(this.writesPerPartition));
            this.partitionIndexes.put(writeAddresses.get(i).getPartitionId(), i);
        }

        // Init clients
        for (int j = 0; j < this.clients; j++) {
            try {
                Client c = new Client(readAddress, writeAddresses);
                this.scheduler.schedule(
                        new WriteGeneratorRequest(c), 0, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public void run() {
        this.startSignal.countDown();
        for (int i = 0; i < this.regionPartitions; i++) {
            try {
                this.countDowns.get(i).await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.scheduler.shutdown();
    }

    private class WriteGeneratorRequest implements Runnable {
        private Client client;

        public WriteGeneratorRequest(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            KeyPartition keyPartition = getRandomKey(partitionIndexes.keySet());
            ByteString value = Utils.getRandomByteString(bytes);
            try {
                startSignal.await();
                int counterPos = partitionIndexes.get(keyPartition.getPartitionId());
                if (counters.getAndIncrement(counterPos) < writesPerPartition) {
                    this.client.requestWrite(keyPartition.getKey(), value);
                    scheduler.schedule(
                            new WriteGeneratorRequest(this.client),
                            delay, TimeUnit.MILLISECONDS);
                    countDowns.updateAndGet(counterPos, (countDown) -> {
                        countDown.countDown();
                        return countDown;
                    });
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
