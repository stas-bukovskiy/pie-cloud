package com.piecloud.ingredient;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IngredientConverter {

    private final ModelMapper mapper;

    public IngredientConverter() {
        mapper = createModelMapperWithSkippingIdInOneWay();
    }

    private ModelMapper createModelMapperWithSkippingIdInOneWay() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        PropertyMap<IngredientDto, Ingredient> propertyMapWithSkippedIdInDtoToDocWay =
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        skip(destination.getId());
                    }
                };
        mapper.addMappings(propertyMapWithSkippedIdInDtoToDocWay);
        return mapper;
    }

    public IngredientDto convertDocumentToDto(Ingredient ingredient){
        IngredientDto ingredientDto = mapper.map(ingredient, IngredientDto.class);
        if (ingredient.getGroup() != null)
            ingredientDto.getGroup().setId(ingredient.getGroupId());
        log.debug("[INGREDIENT] convert doc {} to dto {} ", ingredient, ingredientDto);
        return ingredientDto;
    }

    public Ingredient convertDtoToDocument(IngredientDto ingredientDto) {
        Ingredient ingredient = mapper.map(ingredientDto, Ingredient.class);
        log.debug("[INGREDIENT] convert dto {} to doc {} ", ingredientDto, ingredient);
        return ingredient;
    }

}
