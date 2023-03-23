
package com.dissertation.referencearchitecture.s3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Helper {
    private final S3Client s3Client;
    private final static String s3Uri = "http://localhost:4566";
    private final static Integer maxKeys = 5;
    private final static String logPrefix = "Logs/";
    private final static String clockPrefix = "Clocks/";

    public S3Helper() throws URISyntaxException {
        this.s3Client = s3Client();
    }

    private static S3Client s3Client() throws URISyntaxException {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        return S3Client.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(s3Uri))
                .forcePathStyle(true)
                .build();
    }

    public boolean persistLog(String bucketName, String timestamp, String logJson) {
        try {   
            createObject(bucketName, logPrefix, timestamp, RequestBody.fromString(logJson));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean persistClock(String bucketName, String timestamp) {
        try {   
            createObject(bucketName, clockPrefix, timestamp, RequestBody.empty());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public S3ReadResponse getLogAfter(String bucketName, String timestamp) {
        List<S3Object> objects = new ArrayList<>();

        try {
            objects = getObjectsAfter(bucketName, logPrefix, timestamp);

            if (objects.isEmpty()) {
                return new S3ReadResponse();
            }

            S3Object last = objects.get(objects.size() - 1);
            if (last.key().compareTo(timestamp) > 0) {
                return getObject(bucketName, last.key());
            }

            return new S3ReadResponse(last.key());
        } catch (Exception e) {
            e.printStackTrace();
            return new S3ReadResponse();
        }

    }

    public String getClocksAfter(String bucketName, String timestamp) {
        List<S3Object> objects = new ArrayList<>();

        try {
            objects = getObjectsAfter(bucketName, logPrefix, timestamp);

            if (objects.isEmpty()) {
                return null;
            }

            S3Object last = objects.get(objects.size() - 1);
            if (last.key().compareTo(timestamp) > 0) {
                return last.key();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    private List<S3Object> getObjectsAfter(String bucketName, String prefix, String key) {
        ListObjectsV2Request listObjects = ListObjectsV2Request
                    .builder()
                    .maxKeys(maxKeys)
                    .prefix(prefix)
                    .startAfter(prefix + key)
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response res = this.s3Client.listObjectsV2(listObjects);
        return res.contents();
    }

    private void createObject(String bucketName, String prefix, String key, RequestBody body) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(prefix + key)
                .build();

        this.s3Client.putObject(objectRequest, body);
    }

    private S3ReadResponse getObject(String bucketName, String key) {   
        GetObjectRequest getObject = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObject);
        return new S3ReadResponse(key, objectBytes.asUtf8String());
    }
}
