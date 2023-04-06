package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.s3.S3Helper;

import io.grpc.Server;

public abstract class ComputeNode {
    protected Server server;
    protected S3Helper s3Helper;
    protected final String id;
    protected ScheduledThreadPoolExecutor scheduler;
    
    public ComputeNode(Server server, ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String id) throws URISyntaxException {
        this.server = server;
        this.scheduler = scheduler;
        this.s3Helper = s3Helper;
        this.id = id;
    }

    public void init() throws IOException, InterruptedException {
        System.out.println("Starting server...");
        this.server.start();
        System.out.println(String.format("Server started at port %d!", this.server.getPort()));
        this.server.awaitTermination();
    }
}
