package com.piecloud.order.line;

import com.piecloud.addition.AdditionDto;
import com.piecloud.pie.PieDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineDto {
    private String id;
    private int amount;
    private BigDecimal price;
    private PieDto pie;
    private AdditionDto addition;
}
