package com.piecloud.order.line;

import com.piecloud.addition.Addition;
import com.piecloud.pie.Pie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.test.StepVerifier;

import java.util.List;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.order.line.RandomOrderLineUtil.countPriceForOrderLine;
import static com.piecloud.order.line.RandomOrderLineUtil.randomOrderLine;
import static com.piecloud.pie.PieUtil.randomPie;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class OrderLineRepositoryTest {

    @TestConfiguration
    static class OrderLineRepositoryTestConfig {
        @Bean
        public OrderLinePriceCounterMongoEventListener priceCounterMongoEventListener() {
            return new OrderLinePriceCounterMongoEventListener();
        }
    }

    @Autowired
    private OrderLineRepository repository;

    private Addition addition;
    private Pie pie;

    @BeforeEach
    void setup() {
        addition = randomAddition(randomAdditionGroup());
        pie = randomPie(List.of(
                randomIngredient(randomIngredientGroup()),
                randomIngredient(randomIngredientGroup())
        ));
    }

    @Test
    void testSaveOrderLineWithAddition_shouldReturn() {
        OrderLine orderLine = randomOrderLine(addition);

        Publisher<OrderLine> setup = repository.deleteAll()
                .then(repository.save(orderLine));

        StepVerifier.create(setup)
                .consumeNextWith(savedOrderLine -> {
                    assertNotNull(savedOrderLine.getId());
                    assertNull(savedOrderLine.getPie());
                    assertEquals(orderLine.getAddition(), savedOrderLine.getAddition());
                    assertEquals(countPriceForOrderLine(orderLine), savedOrderLine.getPrice());
                }).verifyComplete();
    }

    @Test
    void testSaveOrderLineWithPie_shouldReturn() {
        OrderLine orderLine = randomOrderLine(pie);

        Publisher<OrderLine> setup = repository.deleteAll()
                .then(repository.save(orderLine));

        StepVerifier.create(setup)
                .consumeNextWith(savedOrderLine -> {
                    assertNotNull(savedOrderLine.getId());
                    assertNull(savedOrderLine.getAddition());
                    assertEquals(orderLine.getPie(), savedOrderLine.getPie());
                    assertEquals(countPriceForOrderLine(orderLine), savedOrderLine.getPrice());
                }).verifyComplete();
    }


    @Test
    void testFindById_shouldReturnOrderLine() {
        OrderLine orderLine = randomOrderLine(addition);
        String ID = orderLine.getId();

        Publisher<OrderLine> setup = repository.deleteAll()
                .then(repository.save(orderLine))
                .then(repository.findById(ID));

        StepVerifier.create(setup)
                .consumeNextWith(foundPie -> {
                    orderLine.getAddition().setGroup(null);
                    assertEquals(orderLine.getAddition(), foundPie.getAddition());
                })
                .verifyComplete();
    }


}