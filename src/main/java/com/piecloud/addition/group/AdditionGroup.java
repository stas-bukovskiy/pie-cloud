package com.piecloud.addition.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "addition_groups")
public class AdditionGroup {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

}
