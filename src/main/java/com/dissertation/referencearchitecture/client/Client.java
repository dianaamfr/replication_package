package com.dissertation.referencearchitecture.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.dissertation.referencearchitecture.KeyVersion;
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

import io.grpc.ManagedChannel;

public class Client {
    private Map<String, KeyVersion> cache;
    private String lastWriteTimestamp;
    private List<ManagedChannel> channels;
    private ROTServiceGrpc.ROTServiceBlockingStub readStub;
    private Map<Integer, WriteServiceGrpc.WriteServiceBlockingStub> writeStubs;

    public Client(Address readAddress, List<Address> writeAddresses) {
        this.cache = new HashMap<>();
        this.lastWriteTimestamp = Utils.MIN_TIMESTAMP;
        this.channels = new ArrayList<>();
        initStubs(readAddress, writeAddresses);
    }

    public void initStubs(Address readAddress, List<Address> writeAddresses) {
        ManagedChannel readChannel = readAddress.getChannel();
        this.channels.add(readChannel);
        this.readStub = ROTServiceGrpc.newBlockingStub(readChannel);
        this.writeStubs = new HashMap<>();

        for (Address address : writeAddresses) {
            ManagedChannel writeChannel = address.getChannel();
            this.channels.add(writeChannel);
            this.writeStubs.put(address.getPartitionId(), WriteServiceGrpc.newBlockingStub(writeChannel));
        }
    }

    public ROTResponse requestROT(Set<String> keys) {
        Builder builder = ROTResponse.newBuilder().setError(false);
        ROTRequest rotRequest;
        ROTResponse rotResponse;
        Map<String, KeyVersion> versions = new HashMap<>();

        try {
            for (String key : keys) {
                if (!this.isKeyInPartition(key)) {
                    throw new KeyNotFoundException();
                }
            }

            rotRequest = ROTRequest.newBuilder().addAllKeys(keys).build();
            rotResponse = this.readStub.rot(rotRequest);

            // TODO: it will never throw error
            if (!rotResponse.getError()) {
                pruneCache(rotResponse.getStableTime());

                // Search cache
                for (Entry<String, KeyVersion> entry : rotResponse.getVersionsMap().entrySet()) {
                    KeyVersion cacheVersion = this.cache.getOrDefault(entry.getKey(), null);
                    versions.put(entry.getKey(), cacheVersion != null ? cacheVersion : entry.getValue());
                }
                builder.putAllVersions(versions).setStableTime(rotResponse.getStableTime()).setId(rotResponse.getId());

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
            if (!this.isKeyInPartition(key)) {
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
                KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                        .build();
                this.cache.put(key, version);
            }
            return writeResponse;
        } catch (KeyNotFoundException e) {
            return WriteResponse.newBuilder()
                    .setStatus(e.toString())
                    .setError(true)
                    .build();
        }
    }

    public WriteResponse requestCompareVersionAndWrite(String key, ByteString value, String expectedVersion,
            ByteString expectedValue) {
        try {
            int partitionId = Utils.getKeyPartitionId(key);
            if (!this.isKeyInPartition(key)) {
                throw new KeyNotFoundException();
            }

            WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
            WriteRequest.Builder writeRequestBuilder = WriteRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setLastWriteTimestamp(this.lastWriteTimestamp)
                    .setExpectedVersion(expectedVersion);

            if (expectedValue != null) {
                writeRequestBuilder.setExpectedValue(expectedValue);
            }

            WriteResponse writeResponse = writeStub.atomicWrite(writeRequestBuilder.build());
            if (!writeResponse.getError()) {
                this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
                KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                        .build();
                this.cache.put(key, version);
            }
            return writeResponse;
        } catch (KeyNotFoundException e) {
            return WriteResponse.newBuilder()
                    .setStatus(e.toString())
                    .setError(true)
                    .build();
        }
    }

    public WriteResponse requestCompareValueAndWrite(String key, ByteString value, ByteString expectedValue) {
        try {
            int partitionId = Utils.getKeyPartitionId(key);
            if (!this.isKeyInPartition(key)) {
                throw new KeyNotFoundException();
            }

            WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
            WriteRequest.Builder writeRequestBuilder = WriteRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setLastWriteTimestamp(this.lastWriteTimestamp)
                    .setExpectedValue(expectedValue);

            WriteResponse writeResponse = writeStub.atomicWrite(writeRequestBuilder.build());
            if (!writeResponse.getError()) {
                this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
                KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                        .build();
                this.cache.put(key, version);
            }
            return writeResponse;
        } catch (KeyNotFoundException e) {
            return WriteResponse.newBuilder()
                    .setStatus(e.toString())
                    .setError(true)
                    .build();
        }
    }

    // public ReadVersionResponse requestReadVersion(String key, String version) {
    //     ReadVersionRequest readVersionRequest = ReadVersionRequest.newBuilder()
    //             .setKey(key)
    //             .setVersion(version)
    //             .build();

    //     ReadVersionResponse readVersionResponse = this.readStub.readVersion(readVersionRequest);

    //     // TODO: Does it make sense to search the cache for the version requested?
    //     pruneCache(readVersionResponse.getStableTime());
    //     return readVersionResponse;
    // }
    
    private void pruneCache(String stableTime) {
        List<String> toPrune = new ArrayList<>();
        for (Entry<String, KeyVersion> entry : this.cache.entrySet()) {
            if (entry.getValue().getTimestamp().compareTo(stableTime) <= 0) {
                toPrune.add(entry.getKey());
            }
        }
        this.cache.keySet().removeAll(toPrune);
    }

    public void shutdown() {
        for (ManagedChannel channel : this.channels) {
            try {
                channel.shutdown();
                channel.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isKeyInPartition(String key) {
        return this.writeStubs.containsKey(Utils.getKeyPartitionId(key));
    }

}
