package com.dissertation.eventual.client;

import java.net.URISyntaxException;

import com.dissertation.eventual.s3.S3Helper;
import com.dissertation.eventual.utils.Utils;

import software.amazon.awssdk.regions.Region;

public class Client {
    private final S3Helper s3Helper;
    private final String region;

    public Client(S3Helper s3Helper, String region) {
        this.s3Helper = s3Helper;
        this.region = region;
    }
    
    public static void main( String[] args ) {
        final Region region = Utils.getCurrentRegion();
        try {
            final S3Helper s3Helper = new S3Helper(region);
            new Client(s3Helper, region.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    } 

    public String read(String key) {
        
        return s3Helper.read(getBucketName(key), key).getContent();
    }

    public boolean write(String key, String value) {
        return s3Helper.write(getBucketName(key), key, value);
    }

    private String getBucketName(String key) {
        return Utils.getPartitionBucket(Utils.getKeyPartitionId(key), this.region);
    }
}
