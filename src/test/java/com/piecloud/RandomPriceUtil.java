package com.piecloud;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;

public class RandomPriceUtil {

    private static final Random random = new SecureRandom();

    public static BigDecimal random() {
        return BigDecimal.valueOf(random.nextDouble(100) + 1.0).setScale(2, RoundingMode.HALF_UP);
    }

}
