package com.piecloud.pie;

import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientDto;
import com.piecloud.util.RandomPriceUtil;
import com.piecloud.util.RandomStringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class PieUtil {


    public static Pie randomPie(Collection<Ingredient> ingredients) {
        return new Pie(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomPriceUtil.random(),
                RandomStringUtils.random(100),
                ingredients.stream().map(Ingredient::getId).collect(Collectors.toSet()),
                new HashSet<>(ingredients)
        );
    }

    public static PieDto randomPieDto(List<String> ingredientsId) {
        return new PieDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomStringUtils.random(100),
                RandomPriceUtil.random(),
                ingredientsId.stream()
                        .map(ingredientId -> new IngredientDto(ingredientId, null, null, null, null))
                        .collect(Collectors.toList())
        );
    }

    public static BigDecimal calculatePrice(List<IngredientDto> ingredients) {
        return ingredients.stream()
                .map(IngredientDto::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO.setScale(2), (subtotal, element) -> subtotal = subtotal.add(element));
    }

    public static BigDecimal countPrice(Collection<Ingredient> ingredients) {
        return ingredients.stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO.setScale(2), (subtotal, element) -> subtotal = subtotal.add(element));
    }

    public static BigDecimal countPrice(PieDto pieDto) {
        return pieDto.getIngredients().stream()
                .map(IngredientDto::getPrice)
                .reduce(BigDecimal.ZERO.setScale(2), (subtotal, element) -> subtotal = subtotal.add(element));
    }


}
