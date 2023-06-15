package com.dissertation.eventual.client;

import java.net.URISyntaxException;

import com.dissertation.eventual.s3.S3Helper;
import com.dissertation.eventual.s3.S3ReadResponse;
import com.dissertation.eventual.s3.S3Response;
import com.dissertation.eventual.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class Client {
    private final S3Helper s3Helper;
    private final String region;

    public Client() throws URISyntaxException {
        Region region = Utils.getCurrentRegion();
        this.s3Helper = new S3Helper(region);
        this.region = region.toString();
    }

    public S3ReadResponse read(String key) {
        return s3Helper.read(getBucketName(key), key);
    }

    public S3Response write(String key, String value) {
        return s3Helper.write(getBucketName(key), key, value);
    }

    private String getBucketName(String key) {
        return Utils.getPartitionBucket(Utils.getKeyPartitionId(key), this.region);
    }
}
