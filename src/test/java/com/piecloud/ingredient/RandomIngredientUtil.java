package com.piecloud.ingredient;

import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupDto;
import com.piecloud.util.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RandomIngredientUtil {

    private static final BigDecimal PRICE = BigDecimal.TEN;

    public static Ingredient randomIngredient(IngredientGroup group) {
        return new Ingredient(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomStringUtils.random(100),
                PRICE,
                group.getId(),
                group
        );
    }

    public static IngredientDto randomIngredientDto(String groupId) {
        return new IngredientDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomStringUtils.random(100),
                PRICE,
                new IngredientGroupDto(groupId, "")
        );
    }

    public static List<Ingredient> randomIngredients(IngredientGroup group, int numOfIngredients) {
        List<Ingredient> res = new ArrayList<>();
        for (int i = 0; i < numOfIngredients; i++)
            res.add(randomIngredient(group));
        return res;
    }

}
