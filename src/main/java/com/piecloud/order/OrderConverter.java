package com.piecloud.order;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConverter {

    private final ModelMapper mapper;

    public OrderConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        PropertyMap<OrderDto, Order> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        mapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return mapper;
    }

    public OrderDto convertDocumentToDto(Order order){
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        log.debug("[ORDER] convert doc {} to dto {} ", order, orderDto);
        return orderDto;
    }

    public Order convertDtoToDocument(OrderDto orderDto) {
        Order order = mapper.map(orderDto, Order.class);
        log.debug("[ORDER] convert dto {} to doc {} ", orderDto, order);

        return order;
    }
    
}
