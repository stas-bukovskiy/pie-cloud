package com.piecloud.pie;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PieConverter {

    private final ModelMapper mapper;

    public PieConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        PropertyMap<PieDto, Pie> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        mapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return mapper;
    }

    public PieDto convertDocumentToDto(Pie pie){
        PieDto pieDto = mapper.map(pie, PieDto.class);
        log.debug("converting " + pie + " to dto: " + pieDto);
        return pieDto;
    }

    public Pie convertDtoToDocument(PieDto pieDto) {
        Pie pie = mapper.map(pieDto, Pie.class);
        log.debug("converting " + pieDto + " to document: " + pie);
        return pie;
    }
    
}
