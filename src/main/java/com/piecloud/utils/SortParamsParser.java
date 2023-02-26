package com.piecloud.utils;

import org.springframework.data.domain.Sort;

public class SortParamsParser {

    public static Sort parse(String sortParams) {
        String[] sortParamsArray = sortParams.split(",");
        Sort.Direction direction = sortParamsArray.length > 1 ? Sort.Direction.fromString(sortParamsArray[1]) : Sort.Direction.ASC;
        return Sort.by(direction, sortParamsArray[0]);
    }

}
