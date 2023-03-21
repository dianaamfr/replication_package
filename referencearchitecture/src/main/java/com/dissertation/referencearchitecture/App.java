package com.dissertation.referencearchitecture;

import java.net.URISyntaxException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<Object, Object> {
    private final S3Client s3Client;

    public static void main(String[] args) {
        try {
            App app = new App();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public App() throws URISyntaxException {
        s3Client = DependencyFactory.s3Client();

        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
        listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
    }

    @Override
    public Object handleRequest(final Object input, final Context context) {
        return input;
    }
}
