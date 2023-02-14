package com.piecloud.ingredient.group;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class IngredientGroupConverterTest {

    private static IngredientGroupConverter converter;

    @BeforeAll
    public static void setup() {
        converter = new IngredientGroupConverter();
    }

    @Test
    public void testConvertingDocumentToDto() {
        IngredientGroup documentToConvert = new IngredientGroup("id", "name");
        IngredientGroupDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
    }

    @Test
    public void testConvertingDtoToDocument() {
        IngredientGroupDto dtoToConvert = new IngredientGroupDto("id", "name");
        IngredientGroup convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
    }
}
