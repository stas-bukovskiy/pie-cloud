package com.piecloud.ingredient.group;

import com.piecloud.RandomStringUtils;

import java.util.UUID;

public class RandomIngredientGroupUtil {

    public static IngredientGroup randomIngredientGroup() {
        return new IngredientGroup(
                UUID.randomUUID().toString(),
                RandomStringUtils.random()
        );
    }

    public static IngredientGroupDto randomIngredientGroupDto() {
        return new IngredientGroupDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random()
        );
    }

}
