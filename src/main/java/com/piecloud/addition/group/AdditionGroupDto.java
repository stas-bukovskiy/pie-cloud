package com.piecloud.addition.group;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionGroupDto {

    @Nullable
    private String id;

    @NotNull(message = "name must not be null")
    @Size(min = 3, max = 50, message = "name must have more than 3 and less than 50 characters")
    private String name;

}
