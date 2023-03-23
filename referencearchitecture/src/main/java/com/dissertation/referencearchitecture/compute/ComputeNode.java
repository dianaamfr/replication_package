package com.dissertation.referencearchitecture.compute;

import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.s3.S3Helper;

public abstract class ComputeNode {
    protected S3Helper s3helper;
    protected final String id;
    protected ScheduledThreadPoolExecutor scheduler;
    
    public ComputeNode(ScheduledThreadPoolExecutor scheduler, String id) throws URISyntaxException {
        this.scheduler = scheduler;
        this.s3helper = new S3Helper();
        this.id = id;
    }
}
