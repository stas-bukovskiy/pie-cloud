package com.piecloud.pie;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/pie",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PieController {

    private final PieService service;


    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<PieDto> getPies(@RequestParam(value = "sort", required = false,
            defaultValue = "name,asc") String sortParams) {
        return service.getAllPiesDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
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

    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deletePieGroup(@PathVariable String id) {
        return service.deletePie(id);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<PieDto> postImageToAddition(@PathVariable String id, @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToPie(id, image);
    }

    @DeleteMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<PieDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromPie(id);
    }
}
