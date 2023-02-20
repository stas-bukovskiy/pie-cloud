package com.piecloud.order.line;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class OrderLineRepositoryTest {

    @Autowired
    private OrderLineRepository repository;

    @Test
    void testSaveOrderLine_shouldReturn() {
        OrderLine orderLine = new OrderLine();
    }

}