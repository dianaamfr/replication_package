package com.dissertation.evaluation;

import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.dissertation.eventual.client.Client;
import com.dissertation.eventual.s3.S3ReadResponse;
import com.dissertation.eventual.utils.Utils;
import com.dissertation.evaluation.logs.Log;
import com.dissertation.evaluation.logs.ReadRequestLog;
import com.dissertation.evaluation.logs.ReadResponseLog;

public class BusyReadGenerator {
    private final Client client;
    private final long endMarker;
    private final List<String> keys;
    private final ArrayDeque<Log> logs;
    private long lastPayload;
    private int keyCounter;

    private static final String USAGE = "Usage: BusyReadGenerator <expectedWrites:Int> <key:String>+";

    public BusyReadGenerator(long endMarker, List<String> keys) throws URISyntaxException {
        this.client = new Client();
        this.endMarker = endMarker;
        this.keys = keys;
        this.logs = new ArrayDeque<>(Utils.MAX_LOGS);
        this.lastPayload = Utils.PAYLOAD_START_LONG - 1;
        this.keyCounter = 0;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Utils.logToFile(logs,
                        String.format("%s-%s", Utils.READ_CLIENT_ID, Utils.getCurrentRegion().toString()));
            }
        });
    }

    public static void main(String[] args) {
        List<String> keys = new ArrayList<>();

        if (args.length < 2) {
            System.err.println(USAGE);
            return;
        }

        try {
            long expectedWrites = Long.parseLong(args[0]);
            long endMarker = Utils.PAYLOAD_START_LONG + expectedWrites - 1;
            for (int i = 0; i < args.length; i++) {
                keys.add(args[i]);
            }

            BusyReadGenerator reader = new BusyReadGenerator(endMarker, keys);
            reader.run();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (URISyntaxException e) {
            System.err.println("Failed to create client");
        }
    }

    public void run() {
        S3ReadResponse response;
        long t1, t2;
        long valueLong;

        while (true) {
            String key = keys.get(this.keyCounter % keys.size());
            t1 = System.currentTimeMillis();
            response = this.client.read(key);
            t2 = System.currentTimeMillis();

            if (response.isError() || response.getContent().isBlank()) {
                this.keyCounter = this.incrementKeyCounter();
                continue;
            }

            try {
                valueLong = Long.parseLong(response.getContent());
                if (valueLong > this.lastPayload) {
                    this.lastPayload = valueLong;

                    this.logs.add(new ReadRequestLog(this.lastPayload, t1));
                    this.logs.add(new ReadResponseLog(this.lastPayload, t2));
                }
                if (valueLong == this.endMarker) {
                    break;
                }
                this.keyCounter = this.incrementKeyCounter();
            } catch (NumberFormatException e) {
                continue;
            }
        }
    }

    private int incrementKeyCounter() {
        return (this.keyCounter + 1) % this.keys.size();
    }
}
