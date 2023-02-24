package com.piecloud.ingredient.group;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroupDto;
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
        IngredientGroup documentToConvert = randomIngredientGroup();
        IngredientGroupDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
    }

    @Test
    public void testConvertingDtoToDocument() {
        IngredientGroupDto dtoToConvert = randomIngredientGroupDto();
        IngredientGroup convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
    }
}
