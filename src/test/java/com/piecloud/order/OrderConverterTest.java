package com.piecloud.order;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.piecloud.order.RandomOrderUtil.randomOrder;
import static com.piecloud.order.RandomOrderUtil.randomOrderDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderConverterTest {

    private static OrderConverter converter;


    @BeforeAll
    public static void setup() {
        converter = new OrderConverter();
    }

    @Test
    public void testConvertingDocumentToDto() {
        Order documentToConvert = randomOrder();
        OrderDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getStatus(), convertedDto.getStatus());
        assertEquals(documentToConvert.getOrderLines().size(), convertedDto.getOrderLines().size());
    }

    @Test
    public void testConvertingDtoToDocument() {
        OrderDto dtoToConvert = randomOrderDto();
        Order convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getOrderLines().size(), convertedDocument.getOrderLines().size());
    }

}
