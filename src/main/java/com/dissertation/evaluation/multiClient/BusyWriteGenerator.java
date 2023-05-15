package com.dissertation.evaluation.multiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class BusyWriteGenerator {
    private ExecutorService executor;
    private CountDownLatch startSignal;
    private AtomicLong payload;

    private static final String USAGE = "Usage: BusyWriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <keysPerPartition:Int> <clients:Int>";

    public BusyWriteGenerator(ExecutorService executor, Address readAddress, List<Address> writeAddresses, int keysPerPartition, int clients) {
        this.executor = executor;
        this.startSignal = new CountDownLatch(1);
        this.payload = new AtomicLong(Utils.PAYLOAD_START_LONG);

        List<String> keys = Utils.generateKeys(writeAddresses, keysPerPartition);
        this.init(clients, keysPerPartition, keys, readAddress, writeAddresses);
    
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                executor.shutdownNow();
            }
        });
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();

        if (args.length < 8) {
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

            if (args.length < addressesEndIndex + 2) {
                System.err.println(USAGE);
                return;
            }

            int keysPerPartition = Integer.parseInt(args[addressesEndIndex]);
            int clients = Integer.parseInt(args[addressesEndIndex + 1]);

            ExecutorService executor = Executors.newFixedThreadPool(clients);


            BusyWriteGenerator writer = new BusyWriteGenerator(executor, readAddress, writeAddresses, keysPerPartition, clients);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
            return;
        }
    }

    private void init(int clients, int keysPerPartition, List<String> keys, Address readAddress, List<Address> writeAddresses) {
        for(int i = 0; i < clients; i++) {
            Client client = new Client(readAddress, writeAddresses);
            int startIndex = i % keys.size();
            this.executor.submit(new WriteGeneratorRequest(client, keys, startIndex));
        }
    }

    public void run() {
        this.startSignal.countDown();
    }

    private class WriteGeneratorRequest implements Runnable {
        private final Client client;
        private final List<String> keys;

        public WriteGeneratorRequest(Client client, List<String> keys, int startIndex) {
            this.client = client;
            this.keys = keys;
        }

        @Override
        public void run() {
            String key;
            ByteString value;
            long count = 0;

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                Utils.printException(e);
                return;
            }

            long requestPayload;
            while ((requestPayload = payload.getAndIncrement()) < Utils.PAYLOAD_END_LONG) {
                key = keys.get((int) (count % keys.size()));
                value = Utils.byteStringFromString(String.valueOf(requestPayload));
                
                try {
                    this.client.requestWrite(key, value);
                } catch (Exception e) {
                    Utils.printException(e);
                    continue;
                }
                count++;
            }
        }
    }
}
