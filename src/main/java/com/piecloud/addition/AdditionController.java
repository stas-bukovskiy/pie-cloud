package com.piecloud.addition;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/addition")
public class AdditionController {

    private final AdditionService service;

    @Autowired
    public AdditionController(AdditionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<Addition> getAll() {
        return service.getAllAdditions();
    }

    @GetMapping("/{id}")
    public Mono<Addition> getOne(@PathVariable String id) {
        return service.getAddition(id);
    }

    @PostMapping("/")
    public Mono<Addition> create(@RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.createAddition(additionDtoMono);
    }

    @PutMapping("/{id}")
    public Mono<Addition> update(@PathVariable String id,
                                 @RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.updateAddition(id, additionDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return service.deleteAddition(id);
    }
}
