package com.piecloud.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TestImageFilePart implements FilePart {

    private static final DataBufferFactory factory = new DefaultDataBufferFactory();

    private String fileName = "test.png";


    public TestImageFilePart() {
    }

    public TestImageFilePart(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public @NotNull String filename() {
        return fileName;
    }

    @Override
    public @NotNull Mono<Void> transferTo(@NotNull Path dest) {
        return Mono.empty();
    }

    @Override
    public @NotNull String name() {
        return "image";
    }

    @Override
    public @NotNull HttpHeaders headers() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        return httpHeaders;
    }

    @Override
    public @NotNull Flux<DataBuffer> content() {
        return DataBufferUtils.read(
                new ByteArrayResource("name".getBytes(StandardCharsets.UTF_8)), factory, 1024);

    }
}
