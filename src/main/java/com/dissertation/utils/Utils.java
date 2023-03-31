package com.dissertation.utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public final static String MIN_TIMESTAMP = "0.0";

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
