package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupDto;
import com.piecloud.util.RandomStringUtils;

import java.math.BigDecimal;
import java.util.UUID;

public class RandomAdditionUtil {

    private static final String IMAGE_NAME = "default.png";
    private static final BigDecimal PRICE = BigDecimal.TEN;

    public static Addition randomAddition(AdditionGroup group) {
        return new Addition(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                RandomStringUtils.random(100),
                IMAGE_NAME,
                PRICE,
                group.getId(),
                group
        );
    }

    public static AdditionDto randomAdditionDto(String groupId) {
        return new AdditionDto(
                UUID.randomUUID().toString(),
                RandomStringUtils.random(),
                IMAGE_NAME,
                RandomStringUtils.random(100),
                PRICE,
                new AdditionGroupDto(groupId, "")
        );
    }

}
