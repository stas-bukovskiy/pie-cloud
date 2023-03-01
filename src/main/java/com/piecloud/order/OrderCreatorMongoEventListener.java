package com.piecloud.order;


import com.piecloud.order.line.OrderLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Slf4j
@Component
public class OrderCreatorMongoEventListener extends AbstractMongoEventListener<Order> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Order> event) {
        Order orderSource = event.getSource();
        BigDecimal price = countPrice(orderSource);
        event.getSource().setPrice(price);
        event.getSource().setCreatedDate(new Date());
        log.debug("[ORDER] count price {} and set date for {}", price, event.getSource());

        super.onBeforeConvert(event);
    }

    private BigDecimal countPrice(Order order) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (OrderLine orderLine : order.getOrderLines()) {
            if (orderLine.getPrice() == null)
                log.error("[ORDER] price for order line '{}' is null", orderLine);
            else price = price.add(orderLine.getPrice());
        }
        return price;
    }
}
