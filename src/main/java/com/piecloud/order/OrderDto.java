package com.piecloud.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.order.line.OrderLineDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderDto {
    private String id;
    private Date createdDate;
    private OrderStatus status;
    private BigDecimal price;

    @JsonProperty("order_lines")
    @NotNull(message = "order lines must not be null")
    @Size(min = 1, message = "order must contain at least 1 order line")
    @Valid
    private List<OrderLineDto> orderLines;
}
