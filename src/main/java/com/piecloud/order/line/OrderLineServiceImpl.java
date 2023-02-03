package com.piecloud.order.line;

import com.piecloud.addition.AdditionService;
import com.piecloud.pie.PieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository repository;
    private final PieService pieService;
    private final AdditionService additionService;

    @Autowired
    public OrderLineServiceImpl(OrderLineRepository repository, PieService pieService, AdditionService additionService) {
        this.repository = repository;
        this.pieService = pieService;
        this.additionService = additionService;
    }

    @Override
    public Flux<OrderLine> getALlOrderLines() {
        return repository.findAll();
    }

    @Override
    public Mono<OrderLine> getOrderLine(String id) {
        return Mono.just(id)
                .flatMap(repository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order line with such id = " + id)));
    }

    @Override
    public Mono<OrderLine> createOrderLine(Mono<OrderLineDto> orderLineDtoMono) {
        return orderLineDtoMono
                .flatMap(this::checkOrderLineDto)
                .onErrorStop()
                .flatMap(this::createOrderLineWithSpecificProduct)
                .flatMap(repository::save)
                .doFinally(orderLine -> log.debug("create order line: " + orderLine));
    }

    @Override
    public Mono<OrderLine> updateOrderLine(String id, Mono<OrderLineDto> orderLineDtoMono) {
        return getOrderLine(id)
                .onErrorStop()
                .flatMap(orderLine -> orderLineDtoMono.flatMap(this::checkOrderLineDto))
                .flatMap(this::createOrderLineWithSpecificProduct)
                .map(orderLine -> {
                    orderLine.setId(id);
                    return orderLine;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteOrderLine(String id) {
        return repository.deleteById(id);
    }

    private Mono<OrderLine> createOrderLineWithSpecificProduct(OrderLineDto orderLineDto) {
        if (orderLineDto.getPieId() != null)
            return createOrderLineWithPieId(orderLineDto);
        else if (orderLineDto.getPie() != null)
            return createOrderLineWithPie(orderLineDto);
        else
            return createOrderLineWithAddition(orderLineDto);
    }

    private Mono<OrderLineDto> checkOrderLineDto(OrderLineDto orderLineDtoMono) {
        return Mono.just(orderLineDtoMono)
                .map(orderLineDto -> {
                    if (isOrderLineDtoInvalid(orderLineDto))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "order line must contain only one option: pie, pie_id or addition_id");
                    return orderLineDto;
                });
    }

    private boolean isOrderLineDtoInvalid(OrderLineDto orderLineDto) {
        return false;
//        return !((orderLineDto.getPieId() != null &&
//                (orderLineDto.getAdditionId() != null || orderLineDto.getPie() != null)) ||
//                (orderLineDto.getAdditionId() != null &&
//                        (orderLineDto.getPieId() != null || orderLineDto.getPie() != null)) ||
//                (orderLineDto.getPie() != null &&
//                        (orderLineDto.getPieId() != null || orderLineDto.getAdditionId() != null)));
    }

    private Mono<OrderLine> createOrderLineWithPieId(OrderLineDto orderLineDto) {
        return Mono.just(orderLineDto)
                .zipWith(pieService.getPie(orderLineDto.getPieId()))
                .onErrorStop()
                .map(orderLineDtoAndPie -> OrderLine.builder()
                        .amount(orderLineDtoAndPie.getT1().getAmount())
                        .pie(orderLineDtoAndPie.getT2())
                        .build());
    }

    private Mono<OrderLine> createOrderLineWithPie(OrderLineDto orderLineDto) {
        return Mono.just(orderLineDto)
                .zipWith(pieService.createPie(Mono.just(orderLineDto.getPie())))
                .onErrorStop()
                .map(orderLineDtoAndPie -> OrderLine.builder()
                        .amount(orderLineDtoAndPie.getT1().getAmount())
                        .pie(orderLineDtoAndPie.getT2())
                        .build());
    }

    private Mono<OrderLine> createOrderLineWithAddition(OrderLineDto orderLineDto) {
        return Mono.just(orderLineDto)
                .zipWith(additionService.getAddition(orderLineDto.getAdditionId()))
                .onErrorStop()
                .map(orderLineDtoAndAddition -> OrderLine.builder()
                        .amount(orderLineDtoAndAddition.getT1().getAmount())
//                        .addition(orderLineDtoAndAddition.getT2())
                        .build());
    }
}
