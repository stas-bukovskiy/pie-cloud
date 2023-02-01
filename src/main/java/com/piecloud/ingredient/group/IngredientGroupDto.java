package com.piecloud.ingredient.group;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class IngredientGroupDto {
    private String id;
    @Size(min = 3, message = "name must have more than 3 characters")
    private String name;
}
