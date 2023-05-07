package com.piecloud.addition;

import com.piecloud.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "api/addition",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdditionController {

    private final AdditionService service;
    private final ImageService imageService;


    @Operation(summary = "Get all additions")
    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<AdditionDto> getAllAdditions(@Parameter(description = "id of group to search additions")
                                             @RequestParam(value = "group_id", required = false)
                                             String groupId,
                                             @Parameter(name = "first part is field for sorting, second can be asc or desc")
                                             @RequestParam(value = "sort", required = false, defaultValue = "name,asc")
                                             String sortParams) {
        if (groupId != null)
            return service.getAllAdditionsDtoByGroup(groupId, sortParams);
        return service.getAllAdditionsDto(sortParams);
    }

    @Operation(summary = "Get a addition by its id")
    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionDto> getAddition(@Parameter(description = "id of addition to be searched", required = true)
                                         @PathVariable String id) {
        return service.getAdditionDto(id);
    }

    @Operation(summary = "Create new addition")
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AdditionDto> createAddition(@Parameter(description = "AdditionDto with data to create new addition", required = true)
                                            @RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.createAddition(additionDtoMono);
    }

    @Operation(summary = "Update a addition by its id")
    @PutMapping("/{id}")
    public Mono<AdditionDto> updateAddition(@Parameter(description = "id of addition to be updated", required = true)
                                            @PathVariable String id,
                                            @Parameter(description = "AdditionDto with data to update", required = true)
                                            @RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.updateAddition(id, additionDtoMono);
    }

    @Operation(summary = "Delete a addition by its id")
    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteAddition(@Parameter(description = "id of addition to be deleted", required = true)
                                     @PathVariable String id) {
        return service.deleteAddition(id);
    }


    @Operation(summary = "Add image to addition by its id")
    @PostMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> postImageToAddition(@Parameter(description = "id of addition to which image will be added", required = true)
                                                            @PathVariable String id,
                                                            @Parameter(description = "Image to be added to addition", required = true)
                                                            @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToAddition(id, image)
                .map(imageService::toResponseEntity);
    }

    @Operation(summary = "Delete image from addition by its id")
    @DeleteMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<byte[]>> deleteImageFromAddition(@Parameter(description = "id of addition from which image will be deleted", required = true)
                                                                @PathVariable String id) {
        return service.removeImageFromAddition(id)
                .map(imageService::toResponseEntity);
    }
}
