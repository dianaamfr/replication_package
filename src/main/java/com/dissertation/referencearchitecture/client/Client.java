package com.dissertation.referencearchitecture.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dissertation.referencearchitecture.ROTRequest;
import com.dissertation.referencearchitecture.ROTResponse;
import com.dissertation.referencearchitecture.ROTServiceGrpc;
import com.dissertation.referencearchitecture.WriteRequest;
import com.dissertation.referencearchitecture.WriteResponse;
import com.dissertation.referencearchitecture.WriteServiceGrpc;
import com.dissertation.referencearchitecture.ROTResponse.Builder;
import com.dissertation.referencearchitecture.exceptions.KeyNotFoundException;
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.ROTRequestLog;
import com.dissertation.validation.logs.ROTResponseLog;
import com.dissertation.validation.logs.Log;
import com.dissertation.validation.logs.WriteRequestLog;
import com.dissertation.validation.logs.WriteResponseLog;
import com.dissertation.validation.logs.Log.NodeType;
import com.google.protobuf.ByteString;

public class Client {
    private Map<Integer, WriteServiceGrpc.WriteServiceBlockingStub> writeStubs;
    private ROTServiceGrpc.ROTServiceBlockingStub readStub;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    private ArrayDeque<Log> logs;
    private final String id;

    public Client(Address readAddress, List<Address> writeAddresses, String id) {
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        this.logs = new ArrayDeque<>(Utils.MAX_LOGS);
        this.id = id;
        initStubs(readAddress, writeAddresses);

        if(Utils.VALIDATION_LOGS) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    Utils.logToFile(logs, id);
                }
            });
        }
    }

    public Client(Address readAddress, List<Address> writeAddresses) {
        this(readAddress, writeAddresses, "");
    }

    public void initStubs(Address readAddress, List<Address> writeAddresses) {
        this.readStub = ROTServiceGrpc.newBlockingStub(readAddress.getChannel());
        this.writeStubs = new HashMap<>();

        for (Address address : writeAddresses) {
            this.writeStubs.put(address.getPartitionId(), WriteServiceGrpc.newBlockingStub(address.getChannel()));
        }
    }

    public ROTResponse requestROT(Set<String> keys) {
        long t1 = System.currentTimeMillis();
        Builder builder = ROTResponse.newBuilder();
        ROTRequest rotRequest;
        ROTResponse rotResponse;

        try {
            for (String key : keys) {
                if (!this.writeStubs.containsKey(Utils.getKeyPartitionId(key))) {
                    throw new KeyNotFoundException();
                }
            }

            rotRequest = ROTRequest.newBuilder().addAllKeys(keys).build();
            rotResponse = this.readStub.rot(rotRequest);

            if (!rotResponse.getError()) {
                pruneCache(rotResponse.getStableTime());
                builder.putAllValues(getReadResponse(rotResponse.getValuesMap()))
                        .setStableTime(rotResponse.getStableTime());

                if(Utils.VALIDATION_LOGS) {
                    this.logs.add(new ROTRequestLog(NodeType.CLIENT, id, rotResponse.getId(), t1));
                    this.logs.add(new ROTResponseLog(NodeType.CLIENT, id, rotResponse.getId(), rotResponse.getStableTime()));
                }
                
                return builder.build();
            }
            return rotResponse;
        } catch (KeyNotFoundException e) {
            return builder
                    .setStatus(e.toString())
                    .setError(true).build();
        }
    }

    public WriteResponse requestWrite(String key, ByteString value) {
        long t1 = System.currentTimeMillis();

        try {
            int partitionId = Utils.getKeyPartitionId(key);
            if (!this.writeStubs.containsKey(Utils.getKeyPartitionId(key))) {
                throw new KeyNotFoundException();
            }

            WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
            WriteRequest writeRequest = WriteRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setLastWriteTimestamp(this.lastWriteTimestamp)
                    .build();

            WriteResponse writeResponse = writeStub.write(writeRequest);
            if (!writeResponse.getError()) {
                this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
                this.cache.put(key, new Version(key, value, this.lastWriteTimestamp));

                if(Utils.VALIDATION_LOGS) {
                    logs.add(new WriteRequestLog(NodeType.WRITER, id, writeRequest.getKey(), partitionId, t1));
                    logs.add(new WriteResponseLog(NodeType.WRITER, id, writeRequest.getKey(), partitionId,
                            writeResponse.getWriteTimestamp()));
                }
            }
            return writeResponse;
        } catch (KeyNotFoundException e) {
            return WriteResponse.newBuilder()
                    .setStatus(e.toString())
                    .setError(true)
                    .build();
        }
    }

    private void pruneCache(String stableTime) {
        List<String> toPrune = new ArrayList<>();
        for (Entry<String, Version> entry : this.cache.entrySet()) {
            if (entry.getValue().getTimestamp().compareTo(stableTime) <= 0) {
                toPrune.add(entry.getKey());
            }
        }
        this.cache.keySet().removeAll(toPrune);
    }

    private Map<String, ByteString> getReadResponse(Map<String, ByteString> response) {
        Map<String, ByteString> values = new HashMap<>();

        for (Entry<String, ByteString> entry : response.entrySet()) {
            Version v = this.cache.getOrDefault(entry.getKey(), null);
            values.put(entry.getKey(), v != null ? v.getValue() : entry.getValue());
        }
        return values;
    }

}
