package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PieConverterTest {

    private static final PieConverter converter = new PieConverter();

    private static Set<Ingredient> ingredients;
    private static List<IngredientDto> ingredientsDto;

    @BeforeAll
    static void setup() {
        ingredients = new HashSet<>();
        ingredients.add(new Ingredient());
        ingredients.add(new Ingredient());

        ingredientsDto = new ArrayList<>();
        ingredientsDto.add(new IngredientDto());
        ingredientsDto.add(new IngredientDto());
    }

    @Test
    public void testConvertingDocumentToDto() {
        Pie documentToConvert = new Pie("id", "name", "image.png", BigDecimal.TEN, ingredients);
        PieDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
        assertEquals(documentToConvert.getImageName(), convertedDto.getImageName());
        assertEquals(documentToConvert.getPrice(), convertedDto.getPrice());
    }

    @Test
    public void testConvertingDtoToDocument() {
        PieDto dtoToConvert = new PieDto("id", "name", "image.png", BigDecimal.TEN, ingredientsDto);
        Pie convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
        assertEquals(dtoToConvert.getImageName(), convertedDocument.getImageName());
        assertEquals(dtoToConvert.getPrice(), convertedDocument.getPrice());
    }
    
}
