package com.dissertation.utils;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Address {
    private int port;
    private String ip;
    private int partitionId;

    public Address(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public Address(int port, String ip, int partitionId) {
        this(port, ip);
        this.partitionId = partitionId;
    }

    public int getPort() {
        return this.port;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPartitionId() {
        return this.partitionId;
    }

    public ManagedChannel getChannel() {
        return ManagedChannelBuilder
        .forAddress(this.ip, this.port)
        .usePlaintext()
        .build();
    }

}
