package com.piecloud.order.line;

import com.piecloud.RandomStringUtils;
import com.piecloud.addition.Addition;
import com.piecloud.addition.AdditionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.order.line.RandomOrderLineUtil.*;

@SpringBootTest
class OrderLineServiceTest {

    @Autowired
    private OrderLineService service;
    @Autowired
    private OrderLineConverter converter;
    @MockBean
    private AdditionService additionService;
    @MockBean
    private OrderLineRepository repository;

    @Test
    void createOrderLineWithoutAnyProduct() {
        OrderLineDto orderLineDto = randomOrderLineDto();

        Mono<OrderLine> result = service.createOrderLine(Mono.just(orderLineDto));

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void createOrderLineWithBothProduct() {
        OrderLineDto orderLineDtoWithAddition = randomOrderLineDtoWithAdditionId(RandomStringUtils.random());
        OrderLineDto orderLineDtoPie = randomOrderLineDtoWithPieId(RandomStringUtils.random());
        orderLineDtoPie.setAddition(orderLineDtoWithAddition.getAddition());

        Mono<OrderLine> result = service.createOrderLine(Mono.just(orderLineDtoPie));

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void createOrderLineWithAddition() {
        Addition addition = randomAddition(randomAdditionGroup());
        OrderLine orderLineWithAddition = randomOrderLine(addition);
        OrderLineDto orderLineDtoWithAddition = converter.convertDocumentToDto(orderLineWithAddition);
        Mockito.when(additionService.getAdditionDto(addition.getId())).thenReturn(Mono.just(orderLineDtoWithAddition.getAddition()));
        Mockito.when(repository.save(converter.convertDtoToDocument(orderLineDtoWithAddition))).thenReturn(Mono.just(orderLineWithAddition));

        Mono<OrderLine> result = service.createOrderLine(Mono.just(orderLineDtoWithAddition));

        StepVerifier.create(result)
                .expectNext(orderLineWithAddition)
                .verifyComplete();
    }
}