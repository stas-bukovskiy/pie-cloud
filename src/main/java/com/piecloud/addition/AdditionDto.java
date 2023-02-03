package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionDto {
    private String id;

    @Size(min = 3, message = "name must have more than 3 characters")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "price must be larger than 0")
    @Digits(integer = 3, fraction = 2, message = "price must have maximum 3 integral digits " +
            "and 2 fractional digits")
    private BigDecimal price;

    private AdditionGroup group;
}
