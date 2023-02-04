package com.piecloud.pie;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public Flux<PieDto> getPies() {
        return service.getAllPies();
    }

    @GetMapping("/{id}")
    public Mono<PieDto> getPie(@PathVariable String id) {
        return service.getPie(id);
    }

    @PutMapping("/{id}")
    public Mono<PieDto> updatePieGroup(@PathVariable String id,
                                    @Valid @RequestBody Mono<PieDto> pieDtoMono) {
        return service.updatePie(id, pieDtoMono);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PieDto> createPieGroup(@Valid @RequestBody Mono<PieDto> pieDtoMono) {
        return service.createPie(pieDtoMono);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deletePieGroup(@PathVariable String id) {
        return service.deletePie(id);
    }

}
