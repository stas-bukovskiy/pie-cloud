package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredientDto;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IngredientConverterTest {

    private static IngredientConverter converter;
    private static IngredientGroup group;
    private static IngredientGroupDto groupDto;

    @BeforeAll
    public static void setup() {
        converter = new IngredientConverter();
        group = randomIngredientGroup();
        groupDto = new IngredientGroupDto(group.getId(), group.getName());
    }

    @Test
    public void testConvertingDocumentToDto() {
        Ingredient documentToConvert = randomIngredient(group);
        IngredientDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
        assertEquals(documentToConvert.getPrice(), convertedDto.getPrice());
        assertEquals(groupDto, convertedDto.getGroup());
    }

    @Test
    public void testConvertingDtoToDocument() {
        IngredientDto dtoToConvert = randomIngredientDto(group.getId());
        Ingredient convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
        assertEquals(dtoToConvert.getPrice(), convertedDocument.getPrice());
        assertEquals(group.getId(), convertedDocument.getGroup().getId());
    }
    
}
