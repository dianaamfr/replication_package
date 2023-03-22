
package com.dissertation.s3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class S3Helper {
    private final S3Client s3Client;
    private final static String s3Uri = "http://localhost:4566";
    private final static String prefix = "Logs/";
    private final static Integer maxKeys = 5;

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

    public void listBuckets() {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = this.s3Client.listBuckets(listBucketsRequest);
        listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
    }

    public void createBucket(String bucketName) {
        try {
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

            this.s3Client.createBucket(bucketRequest);

        } catch (BucketAlreadyOwnedByYouException e) {
            System.out.println(bucketName +" is ready");
        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }

    public void createObject(String bucketName, String key, String body) {
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(prefix + key)
                .build();

            this.s3Client.putObject(objectRequest, RequestBody.fromString(body));
        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }

    public S3ReadResponse getObjectAfter(String bucketName, String key) {
        List<S3Object> objects = new ArrayList<>();
            
        try {
            ListObjectsV2Request listObjects = ListObjectsV2Request
                .builder()
                .maxKeys(maxKeys)
                .prefix(prefix)
                .startAfter(prefix + key)
                .bucket(bucketName)
                .build();

            ListObjectsV2Response res = this.s3Client.listObjectsV2(listObjects); 
            objects = res.contents();

            if(objects.isEmpty()) {
                return new S3ReadResponse();
            }

            S3Object last = objects.get(objects.size() - 1);
            if(last.key().compareTo(key) > 0) {
                return getObject(bucketName, last.key());
            }
            
            return new S3ReadResponse(last.key());

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        }
    }

    public S3ReadResponse getObject(String bucketName, String key) {
        GetObjectRequest getObject = GetObjectRequest
            .builder()
            .bucket(bucketName)
            .key(key)
            .build();
        
        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObject);
        return new S3ReadResponse(key, objectBytes.asUtf8String());
    }
}
