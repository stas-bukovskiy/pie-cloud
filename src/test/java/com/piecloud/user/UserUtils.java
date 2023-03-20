package com.piecloud.user;

import com.piecloud.util.RandomStringUtils;

import java.util.List;
import java.util.UUID;

public class UserUtils {

    public static User randomUser() {
        return new User(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomStringUtils.random(),
                RandomStringUtils.randomEmail(),
                true,
                List.of("USER")
        );
    }

    public static UserDto randomUserDto() {
        return new UserDto(
                RandomStringUtils.random(),
                RandomStringUtils.random(),
                RandomStringUtils.randomEmail()
        );
    }

}
