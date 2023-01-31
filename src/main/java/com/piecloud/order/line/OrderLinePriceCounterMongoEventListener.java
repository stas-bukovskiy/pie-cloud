package com.piecloud.order.line;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class OrderLinePriceCounterMongoEventListener extends AbstractMongoEventListener<OrderLine> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<OrderLine> event) {
        BigDecimal price = countPriceForOrderLine(event.getSource());
        event.getSource().setPrice(price);
        log.debug("counted price for order line: " + event.getSource());

        super.onBeforeConvert(event);
    }

    private BigDecimal countPriceForOrderLine(OrderLine orderLine) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = BigDecimal.valueOf(orderLine.getAmount());
        if (orderLine.getAddition() != null) {
            price = price.add(orderLine.getAddition().getPrice().multiply(amount));
        } else if (orderLine.getPie() != null) {
            price = price.add(orderLine.getPie().getPrice().multiply(amount));
        }
        return price;
    }
}
