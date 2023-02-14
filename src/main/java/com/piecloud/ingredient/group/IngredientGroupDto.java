package com.piecloud.ingredient.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientGroupDto {

    private String id;

    @NotNull(message = "ingredient name must not be null")
    @Size(min = 3, message = "ingredient name must have more than 3 characters")
    private String name;
}
