package com.piecloud.pie;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/pie")
public class PieController {
    
    private final PieService service;

    @Autowired
    public PieController(PieService service) {
        this.service = service;
    }

    @GetMapping("/")
    public Flux<Pie> getPies() {
        return service.getAllPies();
    }

    @GetMapping("/{id}")
    public Mono<Pie> getPie(@PathVariable String id) {
        return service.getPie(id);
    }

    @PutMapping("/{id}")
    public Mono<Pie> updatePieGroup(@PathVariable String id,
                                    @Valid @RequestBody PieDto pieDto) {
        return service.updatePie(id, pieDto);
    }

    @PostMapping("/")
    public Mono<Pie> createPieGroup(@Valid @RequestBody PieDto pieDto) {
        return service.createPie(pieDto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deletePieGroup(@PathVariable String id) {
        return service.deletePie(id);
    }

}
