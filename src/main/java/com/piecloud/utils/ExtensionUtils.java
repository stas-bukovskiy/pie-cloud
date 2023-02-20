package com.piecloud.utils;

public class ExtensionUtils {

    public static String getFileExtension(String filename) {
        if (filename == null)
            return null;
        String extension = "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex + 1);
        }
        return extension;
    }

}
