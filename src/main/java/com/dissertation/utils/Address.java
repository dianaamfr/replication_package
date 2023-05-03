package com.dissertation.utils;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Address {
    private final int port;
    private final String ip;
    private final int partitionId;

    public Address(int port, String ip, int partitionId) {
        this.port = port;
        this.ip = ip;
        this.partitionId = partitionId;
    }

    public Address(int port, String ip) {
        this(port, ip, -1);
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
