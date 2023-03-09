package com.piecloud.order;

import com.piecloud.addition.Addition;
import com.piecloud.order.line.OrderLine;
import com.piecloud.pie.Pie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

import static com.piecloud.order.OrderStatus.IN_LINE;
import static com.piecloud.order.line.RandomOrderLineUtil.*;

public class RandomOrderUtil {

    private static final Random random = new SecureRandom();

    public static OrderDto randomOrderDto() {
        return new OrderDto(
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                List.of(randomOrderLineDto(), randomOrderLineDto(), randomOrderLineDto())
        );
    }

    public static Order randomOrder() {
        return new Order(
                UUID.randomUUID().toString(),
                new Date(),
                IN_LINE,
                null,
                Set.of(randomOrderLineWithAddition(), randomOrderLineWithAddition(), randomOrderLineWithAddition()),
                UUID.randomUUID().toString()
        );
    }

    public static Order randomOrder(Addition addition, Pie pie, Pie pie2) {
        return new Order(
                UUID.randomUUID().toString(),
                new Date(),
                IN_LINE,
                null,
                Set.of(randomOrderLine(addition), randomOrderLine(pie), randomOrderLine(pie2)),
                UUID.randomUUID().toString()
        );
    }


    public static BigDecimal countPrice(Order order) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (OrderLine orderLine : order.getOrderLines()) {
            price = price.add(orderLine.getPrice());
        }
        return price;
    }

    private static int randomAmount() {
        return random.nextInt(1, 11);
    }


}
