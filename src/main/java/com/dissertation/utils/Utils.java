package com.dissertation.utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public final static String MIN_TIMESTAMP = "0.0";
    public final static int SYNC_DELAY = 10000;
    public final  static int PULL_DELAY = 10000;
    public final  static int CLOCK_DELAY = 20000;

    public static final String LOG_STATE = "state";
    public static final String LOG_VERSIONS = "versions";
    public static final String LOG_KEY = "key";
    public static final String LOG_VALUE = "value";
    public static final String LOG_TIMESTAMP = "timestamp";

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
}
