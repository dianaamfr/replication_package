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
import com.dissertation.utils.Address;
import com.dissertation.utils.Utils;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.Status;

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

    public ROTResponse requestROT(Set<String> keys) throws Exception {
        Builder builder = ROTResponse.newBuilder();
        ROTRequest rotRequest;
        ROTResponse rotResponse;
        Map<String, KeyVersion> versions = new HashMap<>();

        for (String key : keys) {
            if (!this.isKeyInPartition(key)) {
                Status status = Status.INVALID_ARGUMENT.withDescription(String.format("Key %s not found", key));
                throw status.asRuntimeException();
            }
        }

        rotRequest = ROTRequest.newBuilder().addAllKeys(keys).build();
        rotResponse = this.readStub.rot(rotRequest);

        pruneCache(rotResponse.getStableTime());

        // Search cache
        for (Entry<String, KeyVersion> entry : rotResponse.getVersionsMap().entrySet()) {
            KeyVersion cacheVersion = this.cache.getOrDefault(entry.getKey(), null);
            versions.put(entry.getKey(), cacheVersion != null ? cacheVersion : entry.getValue());
        }
        builder.putAllVersions(versions).setStableTime(rotResponse.getStableTime()).setId(rotResponse.getId());

        return builder.build();
    }

    public WriteResponse requestWrite(String key, ByteString value) throws Exception {
        int partitionId = Utils.getKeyPartitionId(key);
        if (!this.isKeyInPartition(key)) {
            Status status = Status.INVALID_ARGUMENT.withDescription(String.format("Key %s not found", key));
            throw status.asRuntimeException();
        }

        WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
        WriteRequest writeRequest = WriteRequest.newBuilder()
                .setKey(key)
                .setValue(value)
                .setLastWriteTimestamp(this.lastWriteTimestamp)
                .build();

        WriteResponse writeResponse = writeStub.write(writeRequest);
        this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
        KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                .build();
        this.cache.put(key, version);
        return writeResponse; 
    }

    public WriteResponse requestCompareVersionAndWrite(String key, ByteString value, String expectedVersion,
            ByteString expectedValue) throws Exception {
        int partitionId = Utils.getKeyPartitionId(key);
        if (!this.isKeyInPartition(key)) {
            Status status = Status.INVALID_ARGUMENT.withDescription(String.format("Key %s not found", key));
            throw status.asRuntimeException();
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
        if(!writeResponse.hasCurrentVersion()) {
            this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
            KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                    .build();
            this.cache.put(key, version);
        }
        return writeResponse;
    }

    public WriteResponse requestCompareValueAndWrite(String key, ByteString value, ByteString expectedValue) throws Exception {
        int partitionId = Utils.getKeyPartitionId(key);
        if (!this.isKeyInPartition(key)) {
            Status status = Status.INVALID_ARGUMENT.withDescription(String.format("Key %s not found", key));
            throw status.asRuntimeException();
        }

        WriteServiceGrpc.WriteServiceBlockingStub writeStub = this.writeStubs.get(partitionId);
        WriteRequest.Builder writeRequestBuilder = WriteRequest.newBuilder()
                .setKey(key)
                .setValue(value)
                .setLastWriteTimestamp(this.lastWriteTimestamp)
                .setExpectedValue(expectedValue);

        WriteResponse writeResponse = writeStub.atomicWrite(writeRequestBuilder.build());
        if(!writeResponse.hasCurrentVersion()) {
            this.lastWriteTimestamp = writeResponse.getWriteTimestamp();
            KeyVersion version = KeyVersion.newBuilder().setTimestamp(this.lastWriteTimestamp).setValue(value)
                    .build();
            this.cache.put(key, version);
        }
        return writeResponse;
    }

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
