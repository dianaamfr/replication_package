package com.dissertation.evaluation.multiClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

import com.dissertation.evaluation.logs.LastStableTimeLog;
import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.ROTCountLog;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

public class ConstantReadGenerator {
    private final ScheduledThreadPoolExecutor scheduler;
    private final long delay;
    private final int readTime;
    private final CountDownLatch countDown;
    private final CountDownLatch startSignal;
    private AtomicLongArray endTimes;
    private AtomicIntegerArray keyCounters;
    private AtomicLong rotCounter;
    private final List<Set<String>> readSets;
    private AtomicReference<String> lastStableTime;
    private final BinaryOperator<String> maxStableTime;
    private long startTime;

    private static final String USAGE = "Usage: ConstantReadGenerator <regionPartitions:Int> " +
            "<readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ " +
            "<delay:Int> <readTime:Int> <keysPerRead:Int> <keysPerPartition:Int> <clients:Int>";

    public ConstantReadGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress,
            List<Address> writeAddresses, long delay, int readTime, int keysPerRead, int keysPerPartition,
            int clients) {
        this.scheduler = scheduler;
        this.delay = delay;
        this.readTime = readTime;
        this.countDown = new CountDownLatch(clients);
        this.startSignal = new CountDownLatch(1);
        this.endTimes = new AtomicLongArray(clients);
        this.keyCounters = new AtomicIntegerArray(clients);
        this.rotCounter = new AtomicLong(0);
        this.readSets = Utils.getReadSets(Utils.generateKeys(writeAddresses, keysPerPartition),
                keysPerRead);
        this.lastStableTime = new AtomicReference<String>(Utils.MIN_TIMESTAMP);
        this.maxStableTime = (t1, t2) -> t1.compareTo(t2) > 0 ? t1 : t2;
        this.init(clients, readAddress, writeAddresses);
    }

    private void init(int clients, Address readAddress, List<Address> writeAddresses) {
        for (int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            this.keyCounters.set(i, i % this.readSets.size());
            this.endTimes.set(i, 0);
            this.scheduler.scheduleWithFixedDelay(new ReadGeneratorRequest(client, i), 0, this.delay,
                    TimeUnit.MILLISECONDS);
        }
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 11) {
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

            if (args.length < addressesEndIndex + 5) {
                System.err.println(USAGE);
                return;
            }

            long delay = Long.parseLong(args[addressesEndIndex]);
            int readTime = Integer.parseInt(args[addressesEndIndex + 1]);
            int keysPerRead = Integer.parseInt(args[addressesEndIndex + 2]);
            int keysPerPartition = Integer.parseInt(args[addressesEndIndex + 3]);
            int clients = Integer.parseInt(args[addressesEndIndex + 4]);
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(clients);

            if ((keysPerPartition * regionPartitions) % keysPerRead != 0) {
                System.err.println(USAGE);
                return;
            }

            ConstantReadGenerator reader = new ConstantReadGenerator(scheduler, readAddress, writeAddresses, delay,
                    readTime, keysPerRead, keysPerPartition, clients);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        }
    }

    private void run() {
        try {
            this.startTime = System.currentTimeMillis();
            this.startSignal.countDown();
            this.countDown.await();
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(5000, TimeUnit.MILLISECONDS);

            ArrayDeque<Log> logs = new ArrayDeque<>(1);
            logs.add(new LastStableTimeLog(this.lastStableTime.get(), this.readTime));
            System.out.println(String.format("ROT count: %d", this.rotCounter.get()));
            logs.add(new ROTCountLog(this.rotCounter.get()));
            Utils.logToFile(logs, String.format("%s-%s", Utils.READ_CLIENT_ID, Utils.getCurrentRegion().toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReadGeneratorRequest implements Runnable {
        private final Client client;
        private int index;

        public ReadGeneratorRequest(Client client, int index) {
            this.client = client;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (endTimes.get(this.index) - startTime < readTime) {
                Set<String> requestKeys = readSets.get(keyCounters.get(this.index));
                ROTResponse rotResponse;
                try {
                    rotResponse = client.requestROT(requestKeys);
                    endTimes.set(this.index, System.currentTimeMillis());
                } catch (Exception e) {
                    incrementKeyCounter(this.index);
                    Utils.printException(e);
                    return;
                }

                rotCounter.accumulateAndGet(1, Long::sum);
                lastStableTime.accumulateAndGet(rotResponse.getStableTime(), maxStableTime);

                if (endTimes.get(this.index) - startTime >= readTime) {
                    countDown.countDown();
                }

                incrementKeyCounter(this.index);
            }
        }
    }

    private void incrementKeyCounter(int index) {
        this.keyCounters.set(index, (this.keyCounters.get(index) + 1) % this.readSets.size());
    }

}
