package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IngredientConverterTest {

    private static IngredientConverter converter;
    private static IngredientGroup group;
    private static IngredientGroupDto groupDto;

    @BeforeAll
    public static void setup() {
        converter = new IngredientConverter();
        group = new IngredientGroup("id", "name");
        groupDto = new IngredientGroupDto("id", "name");
    }

    @Test
    public void testConvertingDocumentToDto() {
        Ingredient documentToConvert = new Ingredient("id", "name", "image.png", BigDecimal.TEN, group);
        IngredientDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
        assertEquals(documentToConvert.getImageName(), convertedDto.getImageName());
        assertEquals(documentToConvert.getPrice(), convertedDto.getPrice());
        assertEquals(groupDto, convertedDto.getGroup());
    }

    @Test
    public void testConvertingDtoToDocument() {
        IngredientDto dtoToConvert = new IngredientDto("id", "name", "image.png", BigDecimal.TEN, groupDto);
        Ingredient convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
        assertEquals(dtoToConvert.getImageName(), convertedDocument.getImageName());
        assertEquals(dtoToConvert.getPrice(), convertedDocument.getPrice());
        assertEquals(group, convertedDocument.getGroup());
    }
    
}
