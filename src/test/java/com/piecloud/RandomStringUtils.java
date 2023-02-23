package com.piecloud;

import java.security.SecureRandom;
import java.util.Random;

public class RandomStringUtils {

    private static final int LEFT_LIMIT = 97; // letter 'a'
    private static final int RIGHT_LIMIT = 122; // letter 'a'
    private static final Random random = new SecureRandom();

    public static String random() {
        return random(10);
    }

    public static String random(int length) {
        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
