package com.piecloud.ingredient.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class IngredientGroupDto {

    private String id;

    @NotNull(message = "ingredient name must not be null")
    @Size(min = 3, message = "ingredient name must have more than 3 characters")
    private String name;
}
