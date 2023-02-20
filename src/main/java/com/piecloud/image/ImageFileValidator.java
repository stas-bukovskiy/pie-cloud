package com.piecloud.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.piecloud.utils.ExtensionUtils.getFileExtension;

@Slf4j
@Component
public class ImageFileValidator  {
    public FilePart checkValidImageFilePart(FilePart fiePart) {
        String filename = fiePart.filename();
        String extension = getFileExtension(filename);
        boolean isSupportedExtension = isSupportedExtension(extension);

        log.debug(String.format("Checking result for %s: %s", filename, isSupportedExtension));

        if (!isSupportedExtension)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PNG JPEG or JPG images are allowed");
        return fiePart;
    }

    private boolean isSupportedExtension(String extension) {
        return extension != null && (extension.equalsIgnoreCase("png")
                || extension.equalsIgnoreCase("jpg")
                || extension.equalsIgnoreCase("jpeg"));
    }
}
