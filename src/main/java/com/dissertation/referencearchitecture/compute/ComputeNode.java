package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.s3.S3Helper;

import io.grpc.Server;

public abstract class ComputeNode {
    protected Server server;
    protected S3Helper s3Helper;
    protected ScheduledThreadPoolExecutor scheduler;
    
    public ComputeNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper) throws URISyntaxException {
        this.scheduler = scheduler;
        this.s3Helper = s3Helper;
    }

    public void init(Server server) throws IOException, InterruptedException {
        this.server = server;
        
        System.out.println("Starting server...");
        this.server.start();
        System.out.println(String.format("Server started at port %d!", this.server.getPort()));
        this.server.awaitTermination();
    }
}
