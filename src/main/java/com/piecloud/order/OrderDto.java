package com.piecloud.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.order.line.OrderLineDto;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class OrderDto {
    @JsonProperty("order_lines")
    @Size(min = 1, message = "order must contain at least 1 order line")
    private Set<OrderLineDto> orderLines;
}
