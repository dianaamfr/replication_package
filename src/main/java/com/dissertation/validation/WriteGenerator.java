package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final int WRITES_PER_PARTITION = 10;

    private AtomicIntegerArray counters;
    private AtomicReferenceArray<CountDownLatch> countDowns;

    private static final String USAGE = "Usage: WriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <writesPerPartition:Int> <bytes:Int> ";

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int bytes, int writesPerPartition) {
        super(scheduler, writeAddresses, regionPartitions, delay);
        this.writesPerPartition = writesPerPartition;
        this.bytes = bytes;

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
          
            WriteGenerator writeGenerator = new WriteGenerator(scheduler, readAddress, writeAddresses, regionPartitions,
                    delay, bytes, writesPerPartition);
            writeGenerator.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }


    private void init(Address readAddress, List<Address> writeAddresses) {
        // Init countdowns and clients
        for (int i = 0; i < this.regionPartitions; i++) {
            CountDownLatch countDown = new CountDownLatch(this.writesPerPartition);
            this.countDowns.set(i, countDown);

            Client c = new Client(readAddress, writeAddresses);
            this.scheduler.scheduleWithFixedDelay(new WriteGeneratorRequest(c, countDown, i), 0, this.delay, TimeUnit.MILLISECONDS);
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
        private final Client client;
        private final CountDownLatch countDown;
        private final int counterPos;

        public WriteGeneratorRequest(Client client, CountDownLatch countDown, int counterPos) {
            this.client = client;
            this.countDown = countDown;
            this.counterPos = counterPos;
        }

        @Override
        public void run() {
            KeyPartition keyPartition = getRandomKey();
            ByteString value = Utils.getRandomByteString(bytes);
            try {
                startSignal.await();
                if (counters.getAndIncrement(counterPos) < writesPerPartition) {
                    this.client.requestWrite(keyPartition.getKey(), value);
                    this.countDown.countDown();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
