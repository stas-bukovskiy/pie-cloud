package com.piecloud.order.line;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderLineConverter {

    private final ModelMapper mapper;

    public OrderLineConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        PropertyMap<OrderLineDto, OrderLine> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        mapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return mapper;
    }

    public OrderLineDto convertDocumentToDto(OrderLine orderLine){
        OrderLineDto orderLineDto = mapper.map(orderLine, OrderLineDto.class);
        log.debug("[ORDER_LINE] convert doc {} to dto {} ", orderLine, orderLineDto);
        return orderLineDto;
    }

    public OrderLine convertDtoToDocument(OrderLineDto orderLineDto) {
        OrderLine orderLine = mapper.map(orderLineDto, OrderLine.class);
        log.debug("[ORDER_LINE] convert dto {} to doc {} ", orderLineDto, orderLine);
        return orderLine;
    }
    
}
