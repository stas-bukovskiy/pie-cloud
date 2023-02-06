package com.piecloud.order.line;

import com.piecloud.addition.AdditionDto;
import com.piecloud.addition.AdditionService;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.pie.PieDto;
import com.piecloud.pie.PieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository repository;
    private final OrderLineConverter converter;
    private final PieService pieService;
    private final AdditionService additionService;

    @Autowired
    public OrderLineServiceImpl(OrderLineRepository repository,
                                OrderLineConverter converter,
                                PieService pieService,
                                AdditionService additionService) {
        this.repository = repository;
        this.converter = converter;
        this.pieService = pieService;
        this.additionService = additionService;
    }


    @Override
    public Flux<OrderLine> getALlOrderLines() {
        return repository.findAll();
    }

    @Override
    public Mono<OrderLine> getOrderLine(String id) {
        checkOrderLineId(id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "not found order line with such id = " + id)));
    }

    @Override
    public Mono<OrderLine> createOrderLine(Mono<OrderLineDto> orderLineDtoMono) {
        return orderLineDtoMono
                .flatMap(this::checkOrderLineDto)
                .flatMap(this::createOrderLineWithSpecificProduct)
                .flatMap(repository::save)
                .doFinally(orderLine -> log.debug("create order line: " + orderLine));
    }

    @Override
    public Mono<OrderLine> updateOrderLine(String id, Mono<OrderLineDto> orderLineDtoMono) {
        return getOrderLine(id)
                .zipWith(orderLineDtoMono
                        .flatMap(this::checkOrderLineDto)
                        .flatMap(this::createOrderLineWithSpecificProduct)
                )
                .map(orderLineOrderLineTuple2 -> {
                    OrderLine lineToBeUpdated = orderLineOrderLineTuple2.getT1();
                    OrderLine orderLineDto = orderLineOrderLineTuple2.getT2();
                    lineToBeUpdated.setAmount(orderLineDto.getAmount());
                    lineToBeUpdated.setAddition(orderLineDto.getAddition());
                    lineToBeUpdated.setPie(orderLineDto.getPie());
                    return lineToBeUpdated;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<Void> deleteOrderLine(String id) {
        checkOrderLineId(id);
        return repository.deleteById(id);
    }

    private Mono<OrderLineDto> checkOrderLineDto(OrderLineDto dto) {
        return Mono.just(dto)
                .map(orderLineDto -> {
                    if (isOrderLineDtoInvalid(orderLineDto))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "order line must contain only one option: pie or addition");
                    return orderLineDto;
                });
    }

    private boolean isOrderLineDtoInvalid(OrderLineDto orderLineDto) {
        return (orderLineDto.getPie() != null && orderLineDto.getAddition() != null) ||
                (orderLineDto.getPie() == null && orderLineDto.getAddition() == null);
    }

    private Mono<OrderLine> createOrderLineWithSpecificProduct(OrderLineDto orderLineDto) {
        if (orderLineDto.getPie() != null)
            return createOrderLineWithPie(orderLineDto);
        else
            return createOrderLineWithAddition(orderLineDto);
    }

    private Mono<OrderLine> createOrderLineWithPie(OrderLineDto dto) {
        return Mono.just(dto)
                .zipWhen(orderLineDto -> {
                    if (orderLineDto.getPie().getId() != null)
                        return pieService.getPieDto(orderLineDto.getPie().getId());
                    else if (isValidOrderLineDtoWithPie(orderLineDto)) {
                        return pieService.createPie(Mono.just(orderLineDto.getPie()));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "invalid order line with pie: should give pie id or ingredients for pie"));
                })
                .map(orderLineDtoAndPieDto -> {
                    OrderLineDto orderLineDto = orderLineDtoAndPieDto.getT1();
                    PieDto pieDto = orderLineDtoAndPieDto.getT2();
                    orderLineDto.setPie(pieDto);
                    return orderLineDto;
                })
                .map(converter::convertDtoToDocument);
    }

    private boolean isValidOrderLineDtoWithPie(OrderLineDto orderLineDto) {
        List<IngredientDto> pieIngredientsFto = orderLineDto.getPie().getIngredients();
        return pieIngredientsFto != null && pieIngredientsFto.size() >= 1;
    }

    private Mono<OrderLine> createOrderLineWithAddition(OrderLineDto dto) {
        return Mono.just(dto)
                .zipWhen(orderLineDto -> additionService.getAdditionDto(orderLineDto.getAddition().getId()))
                .map(orderLineDtoAndAdditionDto -> {
                    OrderLineDto orderLineDto = orderLineDtoAndAdditionDto.getT1();
                    AdditionDto additionDto = orderLineDtoAndAdditionDto.getT2();
                    orderLineDto.setAddition(additionDto);
                    return orderLineDto;
                })
                .map(converter::convertDtoToDocument);
    }

    private void checkOrderLineId(String id) {
        if (id == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "order line id must not be null");
    }


}
