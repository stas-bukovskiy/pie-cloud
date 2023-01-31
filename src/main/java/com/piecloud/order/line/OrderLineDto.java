package com.piecloud.order.line;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.piecloud.pie.PieDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderLineDto {

    private int amount;

    private PieDto pie;

    @JsonProperty("pie_id")
    private String pieId;

    @JsonProperty("addition_id")
    private String additionId;
}
