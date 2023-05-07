package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AdditionConverterTest {

    private static AdditionConverter converter;
    private static AdditionGroup group;
    private static AdditionGroupDto groupDto;

    @BeforeAll
    public static void setup() {
        converter = new AdditionConverter();
        group = new AdditionGroup("id", "name");
        groupDto = new AdditionGroupDto("id", "name");
    }

    @Test
    public void testConvertingDocumentToDto() {
        Addition documentToConvert = new Addition("id", "name", "description", BigDecimal.TEN, group.getId(), group);
        AdditionDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getName(), convertedDto.getName());
        assertEquals(documentToConvert.getPrice(), convertedDto.getPrice());
        assertEquals(groupDto, convertedDto.getGroup());
    }

    @Test
    public void testConvertingDtoToDocument() {
        AdditionDto dtoToConvert = new AdditionDto("id", "name", "description", BigDecimal.TEN, groupDto);
        Addition convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getName(), convertedDocument.getName());
        assertEquals(dtoToConvert.getPrice(), convertedDocument.getPrice());
        assertEquals(group, convertedDocument.getGroup());
    }
    
}
