package com.dissertation.validation;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.utils.Utils;

public class BusyWriteGenerator {
    private final Client client;
    private final List<String> keys;

    private static final String USAGE = "Usage: BusyWriteGenerator <key:String>+";

    public BusyWriteGenerator(List<String> keys) throws URISyntaxException {
        this.client = new Client();
        this.keys = keys;
    }

    public static void main(String[] args) {
        List<String> keys = new ArrayList<>();

        if (args.length < 1) {
            System.err.println(USAGE);
            return;
        }

        for (int i = 0; i < args.length; i++) {
            keys.add(args[i]);
        }

        try {
            BusyWriteGenerator writer = new BusyWriteGenerator(keys);
            writer.run();
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    public void run() {
        String key;
        long payload = Utils.PAYLOAD_START_LONG;
        long count = 0;

        while (payload < Utils.PAYLOAD_END_LONG) {
            key = keys.get((int) (count % keys.size()));
            this.client.write(key, String.valueOf(payload));
            payload++;
        }
    }

}
