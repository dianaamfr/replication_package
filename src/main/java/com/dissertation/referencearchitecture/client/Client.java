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

import com.google.protobuf.ByteString;

public class Client {
    private Map<Integer, WriteServiceGrpc.WriteServiceBlockingStub> writeStubs;
    private ROTServiceGrpc.ROTServiceBlockingStub readStub;
    private Map<String, Version> cache;
    private String lastWriteTimestamp;

    public Client(Address readAddress, List<Address> writeAddresses) {
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        initStubs(readAddress, writeAddresses);
    }

    public void initStubs(Address readAddress, List<Address> writeAddresses) {
        this.readStub = ROTServiceGrpc.newBlockingStub(readAddress.getChannel());
        this.writeStubs = new HashMap<>();

        for (Address address : writeAddresses) {
            this.writeStubs.put(address.getPartitionId(), WriteServiceGrpc.newBlockingStub(address.getChannel()));
        }
    }

    public ROTResponse requestROT(Set<String> keys) {
        Builder builder = ROTResponse.newBuilder();
        ROTRequest rotRequest;
        ROTResponse rotResponse;
        Map<String, ByteString> values = new HashMap<>();

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

                // Search cache
                for (Entry<String, ByteString> entry : rotResponse.getValuesMap().entrySet()) {
                    Version cacheVersion = this.cache.getOrDefault(entry.getKey(), null);
                    ByteString version = cacheVersion != null ? cacheVersion.getValue() : entry.getValue();
                    values.put(entry.getKey(), version);
                }
                builder.putAllValues(values).setStableTime(rotResponse.getStableTime());
                
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

}
