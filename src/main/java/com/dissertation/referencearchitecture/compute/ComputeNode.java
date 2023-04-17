package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;
import com.dissertation.validation.logs.Log;

import io.grpc.Server;

public abstract class ComputeNode {
    protected Server server;
    protected S3Helper s3Helper;
    protected ScheduledThreadPoolExecutor scheduler;
    protected String id;
    protected ArrayDeque<Log> logs;
    protected ArrayDeque<Log> s3Logs;

    public ComputeNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String id) throws URISyntaxException {
        this.scheduler = scheduler;
        this.s3Helper = s3Helper;
        this.id = id;
        this.logs = new ArrayDeque<>(Utils.MAX_LOGS);
        this.s3Logs = new ArrayDeque<>(50);

        if(Utils.VALIDATION_LOGS) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    scheduler.shutdown();
                    Utils.logToFile(s3Logs, id + "-s3");
                    Utils.logToFile(logs, id);
                }
            });
        }
    }

    public void init(Server server) throws IOException, InterruptedException {
        this.server = server;

        System.out.println("Starting server...");
        this.server.start();
        System.out.println(String.format("Server started at port %d!", this.server.getPort()));
        this.server.awaitTermination();
    }
}
