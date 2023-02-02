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
        log.debug("converting " + ingredientGroup + " to dto");
        return modelMapper.map(ingredientGroup, IngredientGroupDto.class);
    }

    public IngredientGroup convertDtoToDocument(IngredientGroupDto ingredientGroupDto) {
        log.debug("converting " + ingredientGroupDto + " to document");
        return modelMapper.map(ingredientGroupDto, IngredientGroup.class);
    }

}
