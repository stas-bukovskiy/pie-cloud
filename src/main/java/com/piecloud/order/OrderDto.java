package com.piecloud.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.order.line.OrderLineDto;
import jakarta.annotation.Nullable;
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

    @Nullable
    private String id;

    @Nullable
    private Date createdDate;

    @Nullable
    private OrderStatus status;

    @Nullable
    private BigDecimal price;

    @JsonProperty("order_lines")
    @NotNull(message = "order lines must not be null")
    @Size(min = 1, message = "order must contain at least 1 order line")
    @Valid
    private List<OrderLineDto> orderLines;
}
