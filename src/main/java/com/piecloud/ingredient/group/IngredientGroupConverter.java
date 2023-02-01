package com.piecloud.ingredient.group;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IngredientGroupConverter {

    private final ModelMapper modelMapper;
    private final ModelMapper modelMapperWithIgnoredId;

    public IngredientGroupConverter() {
        this.modelMapper = new ModelMapper();

        this.modelMapperWithIgnoredId = new ModelMapper();
        this.modelMapperWithIgnoredId.getConfiguration().setAmbiguityIgnored(true);
        PropertyMap<IngredientGroupDto, IngredientGroup> clientPropertyMap =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        this.modelMapperWithIgnoredId.addMappings(clientPropertyMap);


    }

    public IngredientGroupDto convertDocumentToDto(IngredientGroup ingredientGroup){
        log.debug("converting " + ingredientGroup + " to dto");
        return modelMapper.map(ingredientGroup, IngredientGroupDto.class);
    }

    public IngredientGroup convertDtoToDocument(IngredientGroupDto ingredientGroupDto) {
        log.debug("converting " + ingredientGroupDto + " to document");
        return modelMapperWithIgnoredId.map(ingredientGroupDto, IngredientGroup.class);
    }

}
