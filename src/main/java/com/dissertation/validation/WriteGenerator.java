package com.dissertation.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class WriteGenerator extends LoadGenerator {
    private final int bytes;
    private final int writesPerPartition;

    private static final int OBJECT_BYTES = 8;
    private static final int WRITES_PER_PARTITION = 15;

    private static final String USAGE = "Usage: WriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String>)+ <delay:Int> <clients:Int> <writesPerPartition:Int> <bytes:Int> ";

    public WriteGenerator(ScheduledThreadPoolExecutor scheduler, Address readAddress, List<Address> writeAddresses,
            int regionPartitions, int delay, int clients, int writesPerPartition, int bytes) {
        super(scheduler, readAddress, writeAddresses, regionPartitions, delay, writesPerPartition, clients);
        this.bytes = bytes;
        this.writesPerPartition = writesPerPartition;
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

    @Override
    protected void init(Address readAddress, List<Address> writeAddresses) {
        super.init(readAddress, writeAddresses);

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

    private class WriteGeneratorRequest implements Runnable {
        private Client client;

        public WriteGeneratorRequest(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            KeyPartition keyPartition = getRandomKey();
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
