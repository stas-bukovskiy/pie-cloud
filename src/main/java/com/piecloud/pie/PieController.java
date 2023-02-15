package com.piecloud.pie;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
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
        return service.getAllPiesDto();
    }

    @GetMapping("/{id}")
    public Mono<PieDto> getPie(@PathVariable String id) {
        return service.getPieDto(id);
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

    @PostMapping(value = "/{id}/image")
    public Mono<PieDto> postImageToAddition(@PathVariable String id, Mono<FilePart> image) {
        return service.addImageToPie(id, image);
    }

    @DeleteMapping("/{id}/image")
    public Mono<PieDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromPie(id);
    }
}
