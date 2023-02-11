package com.piecloud.addition.group;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AdditionGroupConverterTest {

    private static AdditionGroupConverter converter;

    @BeforeAll
    public static void setup() {
        converter = new AdditionGroupConverter();
    }

    @Test
    public void testConvertingDocumentToDto() {
        AdditionGroup documentToConvert = new AdditionGroup("id", "name");
        AdditionGroupDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
    }

    @Test
    public void testConvertingDtoToDocument() {
        AdditionGroupDto dtoToConvert = new AdditionGroupDto("id", "name");
        AdditionGroup convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
    }
}
