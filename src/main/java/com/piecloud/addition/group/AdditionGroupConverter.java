package com.piecloud.addition.group;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdditionGroupConverter {

    private final ModelMapper mapper;

    public AdditionGroupConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        PropertyMap<AdditionGroupDto, AdditionGroup> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        modelMapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return modelMapper;
    }

    public AdditionGroupDto convertDocumentToDto(AdditionGroup additionGroup){
        AdditionGroupDto convertedGroupDto = mapper.map(additionGroup, AdditionGroupDto.class);
        log.debug("[ADDITION_GROUP] convert doc {} to dto {} ", additionGroup, convertedGroupDto);
        return convertedGroupDto;
    }

    public AdditionGroup convertDtoToDocument(AdditionGroupDto additionGroupDto) {
        AdditionGroup convertedGroup = mapper.map(additionGroupDto, AdditionGroup.class);
        log.debug("[ADDITION_GROUP] convert dto {} to doc {} ", additionGroupDto, convertedGroup);
        return convertedGroup;
    }

}
