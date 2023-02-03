package com.piecloud.addition;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdditionConverter {

    private final ModelMapper mapper;


    public AdditionConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        PropertyMap<AdditionDto, Addition> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        mapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return mapper;
    }

    public AdditionDto convertDocumentToDto(Addition addition){
        AdditionDto additionDto = mapper.map(addition, AdditionDto.class);
        log.debug("converting " + addition + " to dto: " + additionDto);
        return additionDto;
    }

    public Addition convertDtoToDocument(AdditionDto additionDto) {
        Addition addition = mapper.map(additionDto, Addition.class);
        log.debug("converting " + additionDto + " to document: " + addition);
        return addition;
    }

}
