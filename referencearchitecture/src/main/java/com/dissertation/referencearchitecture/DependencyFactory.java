
package com.dissertation.referencearchitecture;

import java.net.URI;
import java.net.URISyntaxException;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of S3Client
     * @throws URISyntaxException
     */
    public static S3Client s3Client() throws URISyntaxException {
        URI endpointOverride = new URI("http://0.0.0.0:4566");
        return S3Client.builder()
                       .region(Region.US_WEST_2)
                       .endpointOverride(endpointOverride)
                       .build();
    }
}
