package com.piecloud.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserDto {

    @NotBlank(message = "username is mandatory")
    @Size(min = 4, message = "username minimal length is 4")
    private String username;

    @NotBlank(message = "password is mandatory")
    @Size(min = 4, message = "password minimal length is 4")
    private String password;

    @Email(message = "email must be valid")
    private String email;

}
