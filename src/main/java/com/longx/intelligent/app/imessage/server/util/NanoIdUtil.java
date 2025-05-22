package com.longx.intelligent.app.imessage.server.util;

import java.security.SecureRandom;
import java.util.Random;

public final class NanoIdUtil {
    public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();
    public static final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static final int DEFAULT_SIZE = 21;

    private NanoIdUtil() {
    }

    public static String randomNanoId() {
        return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, 15);
    }

    public static String randomNanoId(Random random, char[] alphabet, int size) {
        if (random == null) {
            throw new IllegalArgumentException("random cannot be null.");
        } else if (alphabet == null) {
            throw new IllegalArgumentException("alphabet cannot be null.");
        } else if (alphabet.length != 0 && alphabet.length < 256) {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be greater than zero.");
            } else {
                int mask = (2 << (int)Math.floor(Math.log((double)(alphabet.length - 1)) / Math.log(2.0))) - 1;
                int step = (int)Math.ceil(1.6 * (double)mask * (double)size / (double)alphabet.length);
                StringBuilder idBuilder = new StringBuilder();

                while(true) {
                    byte[] bytes = new byte[step];
                    random.nextBytes(bytes);

                    for(int i = 0; i < step; ++i) {
                        int alphabetIndex = bytes[i] & mask;
                        if (alphabetIndex < alphabet.length) {
                            idBuilder.append(alphabet[alphabetIndex]);
                            if (idBuilder.length() == size) {
                                return idBuilder.toString();
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("alphabet must contain between 1 and 255 symbols.");
        }
    }
}
