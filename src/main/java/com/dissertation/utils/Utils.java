package com.dissertation.utils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Utils {
    public final static String MIN_TIMESTAMP = "0.0";

    public static byte[] byteArrayFromString(String encodedBuffer) {
        return encodedBuffer.getBytes(StandardCharsets.UTF_8);    
    }
    
    public static String stringFromByteArray(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public static byte[] getRandomByteArray(int sizeInBytes) {
        byte[] b = new byte[sizeInBytes];
        new Random().nextBytes(b);
        return b;
    }
/*     public static String stringFromByteBuffer(ByteBuffer buffer) {
        return stringFromByteArray(buffer.array());
    }

    public static ByteBuffer byteBufferFromString(String encodedBuffer) {
        return ByteBuffer.wrap(byteArrayFromString(encodedBuffer));    
    }

    public static ByteBuffer getRandomByteBuffer(int sizeInBytes) {
        byte[] b = new byte[sizeInBytes];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    } */

}
