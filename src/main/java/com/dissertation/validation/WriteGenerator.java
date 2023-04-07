package com.dissertation.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.google.protobuf.ByteString;

import software.amazon.awssdk.regions.Region;

public class WriteGenerator {

    private ScheduledThreadPoolExecutor scheduler;
    private final int regionPartitions;
    private final int delay;
    private final int bytes;
    private final int writesPerPartition;
    private final int clients;
    private CountDownLatch startSignal;
    private AtomicIntegerArray counters;
    private ConcurrentMap<Integer, CountDownLatch> countDowns;
    private ConcurrentMap<Integer, Integer> partitionCounterPos;

    private static final int MAX_THREADS = 20;
    private static final int OBJECT_BYTES = 8;
    private static final int WRITE_DELAY = 200;
    private static final int WRITES_PER_PARTITION = 15;
    private static final int CLIENTS = 1;
    private static final String USAGE = "Usage: ClientInterface <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <bytes:Int> <writesPerPartition:Int> <clients:Int>";

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, List<Address> writeAddresses,
            int regionPartitions, int delay, int bytes, int writesPerPartition, int clients) {
        this.scheduler = scheduler;

        this.regionPartitions = regionPartitions;
        this.delay = delay;
        this.bytes = bytes;
        this.writesPerPartition = writesPerPartition;
        this.clients = clients;
        this.startSignal = new CountDownLatch(1);
        this.counters = new AtomicIntegerArray((new ArrayList<Integer>(Collections.nCopies(this.regionPartitions, 0)))
                .stream().mapToInt(i -> i).toArray());
        this.countDowns = new ConcurrentHashMap<>();
        this.init(writeAddresses);
    }

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(MAX_THREADS);
        int regionPartitions = 0;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 4) {
            System.err.println(USAGE);
            return;
        }

        try {
            int delay = args.length > regionPartitions + 1 ? Integer.parseInt(args[regionPartitions + 1]) : WRITE_DELAY;
            int bytes = args.length > regionPartitions + 2 ? Integer.parseInt(args[regionPartitions + 2])
                    : OBJECT_BYTES;
            int writesPerPartition = args.length > regionPartitions + 3 ? Integer.parseInt(args[regionPartitions + 3])
                    : WRITES_PER_PARTITION;
            int clients = args.length > regionPartitions + 4 ? Integer.parseInt(args[regionPartitions + 4]) : CLIENTS;
            WriteGenerator writeGenerator = new WriteGenerator(scheduler, writeAddresses, regionPartitions, delay,
                    bytes,
                    writesPerPartition, clients);
            writeGenerator.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void init(List<Address> writeAddresses) {
        // for (int i = 0; i < this.partitions.size(); i++) {
        // CountDownLatch writeSignal = new CountDownLatch(this.writesPerPartition);
        // countDowns.add(writeSignal);
        // for (int j = 0; j < this.clientsPerPartition; j++) {
        // try {
        // Client c = new Client(getRandomRegion(this.partitions.get(i)));
        // this.scheduler.schedule(
        // new PartitionWriteGenerator(c, this.partitions.get(i), i, writeSignal),
        // 0, TimeUnit.MILLISECONDS);
        // } catch (Exception e) {
        // System.err.println(e.toString());
        // }
        // }
        // }
    }

    public void run() {
        // startSignal.countDown();
        // for (int i = 0; i < this.partitions.size(); i++) {
        // try {
        // countDowns.get(i).await();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // this.scheduler.shutdown();
    }

  /*   private Region getRandomRegion(String partition) {
    List<Region> regions = new
    ArrayList<>(Config.getPartitionRegions(partition));
    return regions.get(ThreadLocalRandom.current().nextInt(regions.size()));
    }

    private String getRandomKey(String partition) {
    String result = "";
    int partitionId = Integer.valueOf(partition.split("partition")[1]);
    do {
    result = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) +
    'a'));
    } while ((Math.floorMod(result.hashCode(), this.partitions.size()) + 1) !=
    partitionId);
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
            ByteString value = Utils.getRandomByteString(bytes);
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
    } */
}
