package com.dissertation.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dissertation.referencearchitecture.KeyVersion;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.ROTRequestLog;
import com.dissertation.validation.logs.ROTResponseLog;

public class BusyReadGenerator {
    private final Client client;
    private final long endMarker;
    private final Set<String> keys;
    private final ArrayDeque<Log> logs;
    private long lastPayload;

    private static final String USAGE = "Usage: BusyReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <expectedWrites:Int> <keys:String>";

    public BusyReadGenerator(Address readAddress, List<Address> writeAddresses, long endMarker, Set<String> keys) {
        this.client = new Client(readAddress, writeAddresses);
        this.endMarker = endMarker;
        this.keys = keys;
        this.logs = new ArrayDeque<>(Utils.MAX_LOGS);
        this.lastPayload = Utils.PAYLOAD_START_LONG - 1;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Utils.logToFile(logs,
                        String.format("%s-%s", Utils.READ_CLIENT_ID, Utils.getCurrentRegion().toString()));
            }
        });
    }

    public static void main(String[] args) {
        int regionPartitions = 0;
        Address readAddress;
        List<Address> writeAddresses = new ArrayList<>();
        Set<String> keys = new HashSet<>();

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

            long expectedWrites = Long.parseLong(args[addressesEndIndex]);
            long endMarker = Utils.PAYLOAD_START_LONG + expectedWrites - 1;
            for (int i = addressesEndIndex + 1; i < args.length; i++) {
                keys.add(args[i]);
            }

            BusyReadGenerator reader = new BusyReadGenerator(readAddress, writeAddresses, endMarker, keys);
            reader.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(USAGE);
        }
    }

    public void run() {
        ROTResponse rotResponse;
        boolean newPayload = false;
        boolean exit = false;
        long t1, t2;
        String valueStr;
        long valueLong;

        while (true) {
            t1 = System.currentTimeMillis();
            try {
                rotResponse = this.client.requestROT(this.keys);
                t2 = System.currentTimeMillis();
            } catch (Exception e) {
                Utils.printException(e);
                continue;
            }

            for (KeyVersion keyVersion: rotResponse.getVersionsMap().values()) {
                valueStr = Utils.stringFromByteString(keyVersion.getValue());
                if (valueStr.isBlank()) {
                    continue;
                }

                try {
                    valueLong = Long.parseLong(valueStr);
                    if (valueLong > this.lastPayload) {
                        this.lastPayload = valueLong;
                        newPayload = true;
                    }
                    if(valueLong == this.endMarker) {
                        exit = true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            if (newPayload) {
                newPayload = false;
                this.logs.add(new ROTRequestLog(rotResponse.getId(), t1));
                this.logs.add(new ROTResponseLog(rotResponse.getId(), rotResponse.getStableTime(), t2));
            }

            if(exit) {
                this.client.shutdown();
                break;
            }
        }
    }
}
