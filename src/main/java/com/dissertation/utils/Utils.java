package com.dissertation.utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class Utils {
    public final static String MIN_TIMESTAMP = "0.0";
    public final static int SYNC_DELAY = 10000;
    public final static int PULL_DELAY = 10000;
    public final static int CLOCK_DELAY = 20000;

    public final static Region DEFAULT_REGION = Region.EU_NORTH_1;

    public static byte[] byteArrayFromString(String encodedBuffer) {
        return encodedBuffer.getBytes(StandardCharsets.UTF_8);    
    }
    
    public static String stringFromByteArray(byte[] byteArray) {
        if(byteArray.length == 0) {
            return null;
        }
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public static byte[] getRandomByteArray(int sizeInBytes) {
        byte[] b = new byte[sizeInBytes];
        ThreadLocalRandom.current().nextBytes(b);
        return b;
    }

    public static Region getCurrentRegion() {
        DefaultAwsRegionProviderChain regionLookup = new DefaultAwsRegionProviderChain();
        Region region = Utils.DEFAULT_REGION;

        try {
            region = regionLookup.getRegion();
        } catch (SdkClientException e) {
            System.err.println("Warning: Failed to get default region. Using default."); 
        }
        return region;
    }
}
