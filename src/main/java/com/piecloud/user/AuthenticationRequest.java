package com.piecloud.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class AuthenticationRequest {

    @NotBlank(message = "username is mandatory")
    private String username;
    @NotBlank(message = "password is mandatory")
    private String password;

}
