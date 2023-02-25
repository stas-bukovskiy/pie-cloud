package com.piecloud.ingredient;

import com.piecloud.RandomStringUtils;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;

import java.math.BigDecimal;
import java.util.UUID;

public class RandomIngredientUtil {

    private static final String IMAGE_NAME = "default.png";
    private static final BigDecimal PRICE = BigDecimal.TEN;

    public static Ingredient randomIngredient(IngredientGroup group) {
        return new Ingredient(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                IMAGE_NAME,
                PRICE,
                group
        );
    }

    public static IngredientDto randomIngredientDto(String groupId) {
        return new IngredientDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                IMAGE_NAME,
                PRICE,
                new IngredientGroupDto(groupId, "")
        );
    }

}