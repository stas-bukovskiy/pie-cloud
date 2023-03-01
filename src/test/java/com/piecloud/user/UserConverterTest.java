package com.piecloud.user;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.piecloud.user.UserUtils.randomUser;
import static com.piecloud.user.UserUtils.randomUserDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserConverterTest {

    private static UserConverter converter;

    @BeforeAll
    public static void setup() {
        converter = new UserConverter();
    }

    @Test
    public void testConvertingDocumentToDto() {
        User documentToConvert = randomUser();
        UserDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getUsername(), convertedDto.getUsername());
        assertEquals(documentToConvert.getPassword(), convertedDto.getPassword());
        assertEquals(documentToConvert.getEmail(), convertedDto.getEmail());
    }

    @Test
    public void testConvertingDtoToDocument() {
        UserDto dtoToConvert = randomUserDto();
        User convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertEquals(dtoToConvert.getUsername(), convertedDocument.getUsername());
        assertEquals(dtoToConvert.getPassword(), convertedDocument.getPassword());
        assertEquals(dtoToConvert.getEmail(), convertedDocument.getEmail());
    }
}
    
