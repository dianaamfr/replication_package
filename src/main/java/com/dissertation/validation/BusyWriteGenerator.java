package com.dissertation.validation;

import java.util.ArrayList;
import java.util.List;

import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class BusyWriteGenerator {
    private final Client client;
    private final List<String> keys;

    private static final String USAGE = "Usage: BusyWriteGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <keys:String>";

    public BusyWriteGenerator(Address readAddress, List<Address> writeAddresses, List<String> keys) {
        this.client = new Client(readAddress, writeAddresses);
        this.keys = keys;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                client.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        if (args.length < 7) {
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

            if (args.length < addressesEndIndex + 1) {
                System.err.println(USAGE);
                return;
            }

            for (int i = addressesEndIndex; i < args.length; i++) {
                keys.add(args[i]);
            }

            BusyWriteGenerator writer = new BusyWriteGenerator(readAddress, writeAddresses, keys);
            writer.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
            return;
        }
    }

    public void run() {
        String key;
        ByteString value;
        long payload = Utils.PAYLOAD_START_LONG;
        long count = 0;

        while (payload < Utils.PAYLOAD_END_LONG) {
            key = keys.get((int) (count % keys.size()));
            value = Utils.byteStringFromString(String.valueOf(payload));
            this.client.requestWrite(key, value);
            count++;
            payload++;
        }
    }

}
