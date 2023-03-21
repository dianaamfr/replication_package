package com.dissertation.referencearchitecture.compute;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import software.amazon.awssdk.services.s3.S3Client;

public abstract class ComputeNode {
    protected S3Client s3Client;
    protected ScheduledThreadPoolExecutor scheduler;
    protected final String id;
    protected final String s3Uri = "http://0.0.0.0:4566";

    public ComputeNode(ScheduledThreadPoolExecutor scheduler, String id) {
        this.scheduler = scheduler;
        this.id = id;

        // // Region region = Region.US_EAST_1;
        // try {
        //     URI uri = new URI(s3Uri);
        //     S3Client client = S3Client.builder()
        //                   .region(Region.US_WEST_2)
        //                   .endpointOverride(URI.create("https://s3.us-west-2.amazonaws.com"))
        //                   .forcePathStyle(true)
        //                   .build();
        // } catch (URISyntaxException e) {
        //     e.printStackTrace();
        // }
    }
}
