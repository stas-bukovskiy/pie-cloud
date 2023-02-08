package com.piecloud.addition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.addition.group.AdditionGroup;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionDto {

    private String id;

    @NotNull(message = "addition name must not be null")
    @Size(min = 3, message = "addition name must have more than 3 characters")
    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    @NotNull(message = "addition price must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "addition price must be larger than 0")
    @Digits(integer = 3, fraction = 2, message = "addition price must have maximum 3 integral digits " +
            "and 2 fractional digits")
    private BigDecimal price;

    @NotNull(message = "addition group must not be null")
    private AdditionGroup group;

}
