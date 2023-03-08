package com.piecloud.addition;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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


    @GetMapping(value = "/", consumes = MediaType.ALL_VALUE)
    public Flux<AdditionDto> getAllAdditions(@RequestParam(value = "group_id", required = false) String groupId,
                                             @RequestParam(value = "sort", required = false,
                                                     defaultValue = "name,asc") String sortParams) {
        if (groupId != null)
            return service.getAllAdditionsDtoByGroup(groupId, sortParams);
        return service.getAllAdditionsDto(sortParams);
    }

    @GetMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionDto> getAddition(@PathVariable String id) {
        return service.getAdditionDto(id);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AdditionDto> createAddition(@RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.createAddition(additionDtoMono);
    }

    @PutMapping("/{id}")
    public Mono<AdditionDto> updateAddition(@PathVariable String id,
                                            @RequestBody @Valid Mono<AdditionDto> additionDtoMono) {
        return service.updateAddition(id, additionDtoMono);
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.ALL_VALUE)
    public Mono<Void> deleteAddition(@PathVariable String id) {
        return service.deleteAddition(id);
    }


    @PostMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionDto> postImageToAddition(@PathVariable String id, @RequestPart("image") Mono<FilePart> image) {
        return service.addImageToAddition(id, image);
    }

    @DeleteMapping(value = "/{id}/image", consumes = MediaType.ALL_VALUE)
    public Mono<AdditionDto> deleteImageFromAddition(@PathVariable String id) {
        return service.removeImageFromAddition(id);
    }
}
