package com.piecloud.order.line;

import com.piecloud.addition.AdditionDto;
import com.piecloud.pie.PieDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineDto {

    private String id;

    @NotNull(message = "order line amount must not be null")
    @Min(value = 1, message = "order line amount must be equal or larger than 1")
    private int amount;

    private BigDecimal price;
    private PieDto pie;
    private AdditionDto addition;
}
