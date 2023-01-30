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
    public Mono<Addition> create(@RequestBody @Valid AdditionDto additionDto) {
        return service.createAddition(additionDto);
    }

    @PutMapping("/{id}")
    public Mono<Addition> update(@PathVariable String id,
                                 @RequestBody @Valid AdditionDto additionDto) {
        return service.updateAddition(id, additionDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return service.deleteAddition(id);
    }
}
