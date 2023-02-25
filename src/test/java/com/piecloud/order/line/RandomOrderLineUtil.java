package com.piecloud.order.line;

import com.piecloud.addition.Addition;
import com.piecloud.pie.Pie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class RandomOrderLineUtil {

    private static final Random random = new SecureRandom();

    public static OrderLine randomOrderLine(Addition addition) {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                addition
        );
    }

    public static OrderLine randomOrderLine(Pie pie) {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                pie,
                null
        );
    }

    public static BigDecimal countPriceForOrderLine(OrderLine orderLine) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = BigDecimal.valueOf(orderLine.getAmount());
        if (orderLine.getAddition() != null) {
            price = price.add(orderLine.getAddition().getPrice().multiply(amount));
        } else if (orderLine.getPie() != null) {
            price = price.add(orderLine.getPie().getPrice().multiply(amount));
        }
        return price;
    }

    private static int randomAmount() {
        return random.nextInt(1, 11);
    }


}
