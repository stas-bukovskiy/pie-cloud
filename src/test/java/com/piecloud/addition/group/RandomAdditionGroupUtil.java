package com.piecloud.addition.group;

import com.piecloud.util.RandomStringUtils;

import java.util.UUID;

public class RandomAdditionGroupUtil {

    public static AdditionGroup randomAdditionGroup() {
        return new AdditionGroup(
                UUID.randomUUID().toString(),
                RandomStringUtils.random()
        );
    }

    public static AdditionGroupDto randomAdditionGroupDto() {
        return new AdditionGroupDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random()
        );
    }

}
