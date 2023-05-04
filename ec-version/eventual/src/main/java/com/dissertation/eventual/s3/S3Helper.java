package com.dissertation.eventual.s3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.dissertation.eventual.utils.Utils;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Helper {
    private final S3Client s3Client;

    public S3Helper(Region region) throws URISyntaxException {
        this.s3Client = s3Client(region);
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

    public S3ReadResponse read(String bucketName, String key) {
        try {
            getObject(bucketName, key);
        } catch (Exception e) {
            return new S3Error(e.toString());
        }
        return new S3ReadResponse();
    }

    public boolean write(String bucketName, String key, String value) {
        try {
            createObject(bucketName, key, RequestBody.fromString(value));
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
        return true;
    }

    private S3ReadResponse getObject(String bucketName, String key) {
        GetObjectRequest getObject = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObject);
        return new S3ReadResponse(objectBytes.asUtf8String());
    }

    private void createObject(String bucketName, String key, RequestBody body) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        this.s3Client.putObject(objectRequest, body);
    }
}
