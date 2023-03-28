package com.piecloud.pie;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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


    @Operation(summary = "Get all pies")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<PieDto> getAllPies(@Parameter(name = "first part is field for sorting, second can be asc or desc")
                                   @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
                                   String sortParams) {
        return service.getAllPiesDto(sortParams);
    }

    @Operation(summary = "Get a pie by its id")
    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<PieDto> getPie(@Parameter(description = "id of pie to be searched", required = true)
                               @PathVariable String id) {
        return service.getPieDto(id);
    }

    @Operation(summary = "Update a pie by its id")
    @PutMapping("/{id}")
    public Mono<PieDto> updatePie(@Parameter(description = "id of pie to be updated", required = true)
                                  @PathVariable String id,
                                  @Parameter(description = "PieDto with data to update", required = true)
                                  @Valid @RequestBody Mono<PieDto> pieDtoMono) {
        return service.updatePie(id, pieDtoMono);
    }

    @Operation(summary = "Create new pie")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PieDto> createPie(@Parameter(description = "PieDto with data to create new pie", required = true)
                                  @Valid @RequestBody Mono<PieDto> pieDtoMono) {
        return service.createPie(pieDtoMono);
    }

    @Operation(summary = "Delete a pie by its id")
    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deletePie(@Parameter(description = "id of pie to be deleted", required = true)
                                @PathVariable String id) {
        return service.deletePie(id);
    }

    @Operation(summary = "Add image to pie by its id")
    @PostMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<PieDto> postImageToPie(@Parameter(description = "id of pie to which image will be added", required = true)
                                       @PathVariable String id,
                                       @Parameter(description = "Image to be added to pie", required = true)
                                       @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToPie(id, image);
    }

    @Operation(summary = "Delete image from pie by its id")
    @DeleteMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<PieDto> deleteImageFromPie(@Parameter(description = "id of addition from which image will be deleted", required = true)
                                           @PathVariable String id) {
        return service.removeImageFromPie(id);
    }
}
