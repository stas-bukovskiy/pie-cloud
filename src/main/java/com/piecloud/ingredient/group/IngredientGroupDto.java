package com.piecloud.ingredient.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientGroupDto {
    private String id;
    @NotNull
    @NotBlank
    private String name;
}
