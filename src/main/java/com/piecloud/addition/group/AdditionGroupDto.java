package com.piecloud.addition.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionGroupDto {

    private String id;

    @NotNull(message = "name must not be null")
    @Size(min = 3, message = "name must have more than 3 characters")
    private String name;
}
