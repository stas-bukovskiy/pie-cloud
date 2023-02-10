package com.piecloud.addition;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/addition")
public class AdditionController {

    private final AdditionService service;

    @Autowired
    public AdditionController(AdditionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<AdditionDto> getAll() {
        return service.getAllAdditionsDto();
    }

    @GetMapping("/{id}")
    public Mono<AdditionDto> getOne(@PathVariable String id) {
        return service.getAdditionDto(id);
    }

    @PostMapping("/")
    public Mono<AdditionDto> create(@RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.createAddition(additionDtoMono);
    }

    @PutMapping("/{id}")
    public Mono<AdditionDto> update(@PathVariable String id,
                                    @RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.updateAddition(id, additionDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return service.deleteAddition(id);
    }


    @PostMapping(value = "/{id}/image")
    public Mono<AdditionDto> postImageToAddition(@PathVariable String id, Mono<FilePart> image) {
        return service.addImageToAddition(id, image);
    }

    @DeleteMapping("/{id}/image")
    public Mono<AdditionDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromAddition(id);
    }
}
