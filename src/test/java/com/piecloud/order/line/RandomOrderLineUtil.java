package com.piecloud.order.line;

import com.piecloud.RandomPriceUtil;
import com.piecloud.addition.Addition;
import com.piecloud.addition.AdditionDto;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.pie.Pie;
import com.piecloud.pie.PieDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.piecloud.RandomStringUtils.random;
import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroupDto;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredients;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.pie.PieUtil.randomPieDto;

public class RandomOrderLineUtil {

    private static final Random random = new SecureRandom();

    public static OrderLineDto randomOrderLineDto() {
        return new OrderLineDto(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                null
        );
    }


    public static OrderLineDto randomOrderLineDtoWithAdditionId(String additionId) {
        return new OrderLineDto(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                new AdditionDto(additionId,
                        random(),
                        null,
                        RandomPriceUtil.random(),
                        randomAdditionGroupDto())
        );
    }

    public static OrderLineDto randomOrderLineDtoWithAddition() {
        return new OrderLineDto(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                new AdditionDto(UUID.randomUUID().toString(),
                        random(),
                        null,
                        RandomPriceUtil.random(),
                        randomAdditionGroupDto())
        );
    }

    public static OrderLine randomOrderLineWithAddition() {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                RandomPriceUtil.random(),
                null,
                randomAddition(randomAdditionGroup())
        );
    }

    public static OrderLineDto randomOrderLineDtoWithPieId(String pieId) {
        return new OrderLineDto(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                new PieDto(pieId, null, null, null, null),
                null
        );
    }

    public static OrderLineDto randomOrderLineDtoWithPie() {
        return new OrderLineDto(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                randomPieDto(randomIngredients(randomIngredientGroup(), 3).stream().map(Ingredient::getId).collect(Collectors.toList())),
                null
        );
    }


    public static OrderLine randomOrderLine(Addition addition) {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                addition
        );
    }

    public static OrderLine randomOrderLine() {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                null,
                null
        );
    }

    public static OrderLine randomOrderLine(Pie pie) {
        return new OrderLine(
                UUID.randomUUID().toString(),
                randomAmount(),
                null,
                pie,
                null
        );
    }

    public static BigDecimal countPriceForOrderLine(OrderLine orderLine) {
        BigDecimal price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = BigDecimal.valueOf(orderLine.getAmount());
        if (orderLine.getAddition() != null) {
            price = price.add(orderLine.getAddition().getPrice().multiply(amount));
        } else if (orderLine.getPie() != null) {
            price = price.add(orderLine.getPie().getPrice().multiply(amount));
        }
        return price;
    }

    private static int randomAmount() {
        return random.nextInt(1, 11);
    }


}
