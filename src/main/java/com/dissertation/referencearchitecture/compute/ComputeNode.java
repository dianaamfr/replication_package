package com.dissertation.referencearchitecture.compute;

import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.dissertation.referencearchitecture.s3.S3Helper;

public abstract class ComputeNode {
    protected S3Helper s3Helper;
    protected final String id;
    protected ScheduledThreadPoolExecutor scheduler;
    
    public ComputeNode(ScheduledThreadPoolExecutor scheduler, S3Helper s3Helper, String id) throws URISyntaxException {
        this.scheduler = scheduler;
        this.s3Helper = s3Helper;
        this.id = id;
    }
}
