package com.dissertation.referencearchitecture.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import com.dissertation.utils.record.ROTRecord;
import com.dissertation.utils.record.Record;
import com.dissertation.utils.record.WriteRequestRecord;
import com.dissertation.utils.record.WriteResponseRecord;
import com.dissertation.utils.record.Record.LogType;
import com.dissertation.utils.record.Record.NodeType;
import com.dissertation.utils.record.Record.Phase;
import com.google.protobuf.ByteString;

public class Client {
    private Map<Integer, WriteServiceGrpc.WriteServiceBlockingStub> writeStubs;
    private ROTServiceGrpc.ROTServiceBlockingStub readStub;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;
    private ConcurrentLinkedQueue<Record> logs;
    private final String id;

    public Client(Address readAddress, List<Address> writeAddresses, String id) {
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        this.logs = new ConcurrentLinkedQueue<>();
        this.id = id;
        initStubs(readAddress, writeAddresses);

        if(Utils.ROT_LOGS || Utils.WRITE_LOGS) {
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
        long t1 = System.nanoTime();
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
            long t2 = System.nanoTime();
            rotResponse = this.readStub.rot(rotRequest);
            long t3 = System.nanoTime();

            if (!rotResponse.getError()) {
                pruneCache(rotResponse.getStableTime());
                builder.putAllValues(getReadResponse(rotResponse.getValuesMap()))
                        .setStableTime(rotResponse.getStableTime());
                long t4 = System.nanoTime();
                if(Utils.ROT_LOGS) {
                    this.logs.add(new ROTRecord(NodeType.CLIENT, LogType.ROT_REQUEST, id, rotResponse.getId(), Phase.RECEIVE, t1));
                    this.logs.add(new ROTRecord(NodeType.CLIENT, LogType.ROT_REQUEST, id, rotResponse.getId(), Phase.SEND, t2));
                    this.logs.add(new ROTRecord(NodeType.CLIENT, LogType.ROT_RESPONSE, id, rotResponse.getId(), Phase.RECEIVE, t3));    
                    this.logs.add(new ROTRecord(NodeType.CLIENT, LogType.ROT_RESPONSE, id, rotResponse.getId(),Phase.SEND, t4));
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
        long requestTime = System.nanoTime();

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

                if(Utils.WRITE_LOGS) {
                    logs.add(new WriteRequestRecord(NodeType.WRITER, id, writeRequest.getKey(), partitionId, requestTime));
                    logs.add(new WriteResponseRecord(NodeType.WRITER, id, writeRequest.getKey(), partitionId,
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
