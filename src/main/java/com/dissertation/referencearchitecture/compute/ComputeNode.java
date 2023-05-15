package com.dissertation.referencearchitecture.compute;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.evaluation.logs.Log;
import com.dissertation.referencearchitecture.s3.S3Helper;
import com.dissertation.utils.Utils;

import io.grpc.Server;

public abstract class ComputeNode {
    protected final ScheduledThreadPoolExecutor scheduler;
    protected final S3Helper s3Helper;
    protected Server server;
    protected ArrayDeque<Log> logs;
    protected ArrayDeque<Log> s3Logs;

    public ComputeNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String id) throws URISyntaxException {
        this.scheduler = scheduler;
        this.s3Helper = s3Helper;
        this.logs = new ArrayDeque<>(Utils.MAX_LOGS);
        this.s3Logs = new ArrayDeque<>(Utils.MAX_LOGS);

        if (Utils.GOODPUT_LOGS || Utils.VISIBILITY_LOGS) {
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
