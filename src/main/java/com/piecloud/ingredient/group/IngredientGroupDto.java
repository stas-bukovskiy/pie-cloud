package com.piecloud.ingredient.group;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientGroupDto {

    @Nullable
    private String id;

    @NotNull(message = "ingredient group name must not be null")
    @Size(min = 3, max = 50, message = "ingredient group name must have more than 3 and less that 50 characters")
    private String name;

}
