package com.piecloud.pie;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PieDto {

    private String id;

    @Size(min = 1, message = "pie must contain at least 1 ingredient")
    @JsonProperty("ingredient_ids")
    private Set<String> ingredientIds;

}
