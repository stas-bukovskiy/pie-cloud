package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.group.IngredientGroup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.pie.PieUtil.randomPie;
import static com.piecloud.pie.PieUtil.randomPieDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PieConverterTest {

    private static final PieConverter converter = new PieConverter();

    private static Set<Ingredient> ingredients;

    @BeforeAll
    static void setup() {
        IngredientGroup group = randomIngredientGroup();
        ingredients = new HashSet<>(List.of(
                randomIngredient(group),
                randomIngredient(group),
                randomIngredient(group),
                randomIngredient(group)
        ));
    }

    @Test
    public void testConvertingDocumentToDto() {
        Pie documentToConvert = randomPie(ingredients);
        PieDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
        assertEquals(documentToConvert.getImageName(), convertedDto.getImageName());
        assertEquals(documentToConvert.getPrice(), convertedDto.getPrice());
        assertEquals(documentToConvert.getIngredients().size(), convertedDto.getIngredients().size());
    }

    @Test
    public void testConvertingDtoToDocument() {
        PieDto dtoToConvert = randomPieDto(ingredients.stream().map(Ingredient::getId).collect(Collectors.toList()));
        Pie convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
        assertEquals(dtoToConvert.getImageName(), convertedDocument.getImageName());
        assertEquals(dtoToConvert.getPrice(), convertedDocument.getPrice());
        assertEquals(dtoToConvert.getIngredients().size(), convertedDocument.getIngredients().size());
    }
    
}
