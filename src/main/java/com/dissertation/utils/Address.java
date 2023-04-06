package com.dissertation.utils;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Address {
    private int port;
    private String ip;
    private int partitionId;
    private ManagedChannel channel;

    public Address(int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.initChannel();
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
        return this.channel;
    }

    public void initChannel() {
        this.channel = ManagedChannelBuilder
          .forAddress(this.ip, this.port)
          .usePlaintext()
          .build();
    }
}
