
package com.dissertation.referencearchitecture;

import java.net.URI;
import java.net.URISyntaxException;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class S3Helper {
    private final S3Client s3Client;
    protected final static String s3Uri = "http://localhost:4566";

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
            S3Waiter s3Waiter = this.s3Client.waiter();
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

            this.s3Client.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

            // Wait until the bucket is created and print out the response.
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            System.out.println(bucketName +" is ready");

        } catch (BucketAlreadyOwnedByYouException e) {
            System.out.println(bucketName +" is ready");
        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }

    public void createObject(String bucketName, String key, String body) {
        try {
            S3Waiter s3Waiter = this.s3Client.waiter();
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            this.s3Client.putObject(objectRequest, RequestBody.fromString(body)); 

            HeadObjectRequest objectRequestWait = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

            // Wait until the bucket is created and print out the response.
            WaiterResponse<HeadObjectResponse> waiterResponse = s3Waiter.waitUntilObjectExists(objectRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            System.out.println("Object " + key + " created in " + bucketName);

        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }

    public String getObjectAfter(String bucketName, String key) {
        return "";
    }
}
