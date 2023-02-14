package com.piecloud.ingredient.group;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IngredientGroupConverter {

    private final ModelMapper modelMapper;

    public IngredientGroupConverter() {
        modelMapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        PropertyMap<IngredientGroupDto, IngredientGroup> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        modelMapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return modelMapper;
    }

    public IngredientGroupDto convertDocumentToDto(IngredientGroup ingredientGroup){
        IngredientGroupDto dto = modelMapper.map(ingredientGroup, IngredientGroupDto.class);
        log.debug("converting " + ingredientGroup + " to dto: " + dto);
        return dto;
    }

    public IngredientGroup convertDtoToDocument(IngredientGroupDto ingredientGroupDto) {
        IngredientGroup group = modelMapper.map(ingredientGroupDto, IngredientGroup.class);
        log.debug("converting " + ingredientGroupDto + " to document: " + group);
        return group;
    }

}
