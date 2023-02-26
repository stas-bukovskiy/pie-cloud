package com.piecloud.order.line;

import com.piecloud.addition.AdditionDto;
import com.piecloud.pie.PieDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineDto {

    @Nullable
    private String id;

    @NotNull(message = "order line amount must not be null")
    @Min(value = 1, message = "order line amount must be equal or larger than 1")
    private int amount;

    @Nullable
    private BigDecimal price;

    @Nullable
    private PieDto pie;

    @Nullable
    private AdditionDto addition;
}
