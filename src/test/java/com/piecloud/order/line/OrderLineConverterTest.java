package com.piecloud.order.line;


import com.piecloud.addition.Addition;
import com.piecloud.pie.Pie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.order.line.RandomOrderLineUtil.randomOrderLine;
import static com.piecloud.order.line.RandomOrderLineUtil.randomOrderLineDtoWithPieId;
import static com.piecloud.pie.PieUtil.randomPie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderLineConverterTest {

    private static OrderLineConverter converter;
    private static Addition addition;
    private static Pie pie;

    @BeforeAll
    public static void setup() {
        converter = new OrderLineConverter();
        addition = randomAddition(randomAdditionGroup());
        pie = randomPie(List.of(
                randomIngredient(randomIngredientGroup()),
                randomIngredient(randomIngredientGroup())
        ));
    }

    @Test
    public void testConvertingDocumentToDtoWithAddition() {
        OrderLine documentToConvert = randomOrderLine(addition);
        OrderLineDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getAmount(), convertedDto.getAmount());
        assertEquals(documentToConvert.getAddition().getId(), convertedDto.getAddition().getId());
    }

    @Test
    public void testConvertingDtoToDocumentWithAddition() {
        OrderLineDto dtoToConvert = RandomOrderLineUtil.randomOrderLineDtoWithAdditionId(addition.getId());
        OrderLine convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getAmount(), convertedDocument.getAmount());
        assertEquals(dtoToConvert.getAddition().getId(), convertedDocument.getAddition().getId());
    }

    @Test
    public void testConvertingDocumentToDtoWithAdditionPie() {
        OrderLine documentToConvert = randomOrderLine(pie);
        OrderLineDto convertedDto = converter.convertDocumentToDto(documentToConvert);

        assertEquals(documentToConvert.getId(), convertedDto.getId());
        assertEquals(documentToConvert.getAmount(), convertedDto.getAmount());
        assertEquals(documentToConvert.getPie().getId(), convertedDto.getPie().getId());
        assertEquals(documentToConvert.getPie().getIngredients().size(), convertedDto.getPie().getIngredients().size());
    }

    @Test
    public void testConvertingDtoToDocumentWithAdditionPie() {
        OrderLineDto dtoToConvert = randomOrderLineDtoWithPieId(pie.getId());
        OrderLine convertedDocument = converter.convertDtoToDocument(dtoToConvert);

        assertNull(convertedDocument.getId());
        assertEquals(dtoToConvert.getAmount(), convertedDocument.getAmount());
        assertEquals(dtoToConvert.getPie().getId(), convertedDocument.getPie().getId());
    }


}
