package com.piecloud.image;

import org.modelmapper.Converter;

public class ImageNameToURLConverterGenerator {

    public static Converter<String, String> generate() {
        return ctx -> "http://localhost:8080/img/" + ctx.getSource();
    }

}
