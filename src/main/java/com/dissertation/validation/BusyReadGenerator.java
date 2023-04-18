package com.dissertation.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.client.Client;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.google.protobuf.ByteString;

public class BusyReadGenerator {
    private final Client client;
    private final ByteString endMarker;
    private final Set<String> keys;

    private static final String USAGE = "Usage: ReadGenerator <regionPartitions:Int> <readPort:Int> <readIp:String> (<writePort:Int> <writeIp:String> <partition:Int>)+ <expectedWrites:Int> <keys:String>";

    public BusyReadGenerator(Address readAddress, List<Address> writeAddresses, int endMarker, Set<String> keys) {
        this.client = new Client(readAddress, writeAddresses, String.format("%s-%s", Utils.READ_CLIENT_ID, Utils.getCurrentRegion().toString()));
        this.endMarker = Utils.byteStringFromString(String.valueOf(endMarker));
        this.keys = keys;
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

            if(args.length < addressesEndIndex + 2) {
                System.err.println(USAGE);
                return;
            }

            int expectedWrites = Integer.parseInt(args[addressesEndIndex]);
            int endMarker = Utils.PAYLOAD_START + expectedWrites - 1;
            for (int i = addressesEndIndex + 1; i < args.length; i ++) {
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
        while(true) {
            ROTResponse rotResponse = this.client.requestROT(keys);
            if(!rotResponse.getError() && rotResponse.getValuesMap().containsValue(this.endMarker)) {
                break;
            }
        }
    }
}
