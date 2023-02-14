package com.piecloud;

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
import java.nio.file.Paths;

public class TestImageFilePPart implements FilePart {

    private static final DataBufferFactory factory = new DefaultDataBufferFactory();

    private final static String FILENAME = "test.png";

    @Override
    public String filename() {
        return FILENAME;
    }

    @Override
    public Mono<Void> transferTo(Path dest) {
        return Mono.empty();
    }

    @Override
    public String name() {
        return "image";
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        return httpHeaders;
    }

    @Override
    public Flux<DataBuffer> content() {
        return DataBufferUtils.read(
                new ByteArrayResource("name".getBytes(StandardCharsets.UTF_8)), factory, 1024);

    }
}
