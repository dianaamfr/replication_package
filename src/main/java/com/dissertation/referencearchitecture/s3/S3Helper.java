
package com.dissertation.referencearchitecture.s3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.dissertation.utils.Utils;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Helper {
    private final S3Client s3Client;
    private final S3Client clockS3Client;

    public S3Helper(Region region) throws URISyntaxException {
        this.s3Client = s3Client(region);
        if (Utils.S3_CLOCK_REGION == null || Utils.S3_CLOCK_REGION.isEmpty()) {
            this.clockS3Client = this.s3Client;
        } else {
            this.clockS3Client = s3Client(Region.of(Utils.S3_CLOCK_REGION));
        }
    }

    private static S3Client s3Client(Region region) throws URISyntaxException {
        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(region)
                .forcePathStyle(true);

        if (Utils.S3_ENDPOINT != null && !Utils.S3_ENDPOINT.isEmpty()) {
            return s3ClientBuilder.credentialsProvider(ProfileCredentialsProvider.create())
                    .endpointOverride(URI.create(Utils.S3_ENDPOINT)).build();
        }
        return s3ClientBuilder.credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    public boolean persistLog(String bucketName, String timestamp, String logJson) {
        try {
            createObject(bucketName, Utils.S3_LOG_PREFIX, timestamp, RequestBody.fromString(logJson), this.s3Client);
        } catch (Exception e) {
            System.err.println("Persist log failed: " + e.toString());
            return false;
        }
        return true;
    }

    public boolean persistClock(String timestamp) {
        try {
            createObject(Utils.S3_CLOCK_BUCKET, Utils.S3_CLOCK_PREFIX, timestamp, RequestBody.empty(), this.clockS3Client);
        } catch (Exception e) {
            System.err.println("Persist clock failed: " + e.toString());
            return false;
        }
        return true;
    }

    public S3ReadResponse getLogAfter(String bucketName, String timestamp) {
        List<S3Object> objects;

        try {
            objects = getObjectsAfter(bucketName, Utils.S3_LOG_PREFIX, timestamp, this.s3Client);

            if (objects.isEmpty()) {
                return new S3ReadResponse();
            }

            S3Object last = objects.get(objects.size() - 1);
            if (last.key().compareTo(timestamp) > 0) {
                return getObject(bucketName, last.key(), this.s3Client);
            }

            return new S3ReadResponse();
        } catch (Exception e) {
            return new S3Error(e.toString());
        }

    }

    public S3ReadResponse getClocksAfter(String timestamp) {
        List<S3Object> objects;

        try {
            objects = getObjectsAfter(Utils.S3_CLOCK_BUCKET, Utils.S3_CLOCK_PREFIX, timestamp, this.clockS3Client);

            if (objects.isEmpty()) {
                return new S3ReadResponse();
            }

            S3Object last = objects.get(objects.size() - 1);
            if (last.key().compareTo(timestamp) > 0) {
                String recvTimestamp = last.key().split(Utils.S3_CLOCK_PREFIX)[1];
                return new S3ReadResponse(recvTimestamp);
            }
            return new S3ReadResponse();
        } catch (Exception e) {
            return new S3Error(e.toString());
        }
    }

    private List<S3Object> getObjectsAfter(String bucketName, String prefix, String key, S3Client client) {
        ListObjectsV2Request listObjects = ListObjectsV2Request
                .builder()
                .prefix(prefix)
                .startAfter(prefix + key)
                .bucket(bucketName)
                .build();

        ListObjectsV2Response res = client.listObjectsV2(listObjects);
        return res.contents();
    }

    private void createObject(String bucketName, String prefix, String key, RequestBody body, S3Client client) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(prefix + key)
                .build();

        client.putObject(objectRequest, body);
    }

    private S3ReadResponse getObject(String bucketName, String key, S3Client client) {
        GetObjectRequest getObject = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = client.getObjectAsBytes(getObject);
        String recvTimestamp = key.split(Utils.S3_LOG_PREFIX)[1];
        return new S3ReadResponse(recvTimestamp, objectBytes.asUtf8String());
    }
}
