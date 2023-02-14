package com.piecloud.addition;

import com.piecloud.TestImageFilePart;
import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupDto;
import com.piecloud.addition.group.AdditionGroupRepository;
import com.piecloud.image.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdditionControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AdditionGroupRepository groupRepository;
    @Autowired
    private AdditionRepository repository;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private AdditionConverter converter;
    @Autowired
    private AdditionService service;

    private AdditionGroup group;

    @BeforeEach
    void setup() {
        group = groupRepository.save(new AdditionGroup("id", "name")).block();
    }


    @Test
    public void testPost_shouldReturnAdditionGroup() {
        AdditionGroupDto groupDto = new AdditionGroupDto(group.getId(), "");
        AdditionDto additionDto = new AdditionDto("", "addition", "", BigDecimal.TEN, groupDto);

        webTestClient
                .post()
                .uri("/api/addition/")
                .bodyValue(additionDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AdditionDto.class)
                .value(postedAddition -> {
                    assertNotEquals(additionDto.getId(), postedAddition.getId());
                    assertEquals(additionDto.getName(), postedAddition.getName());
                    assertEquals(imageUploadService.getDefaultImageName(), postedAddition.getImageName());
                    assertEquals(additionDto.getPrice(), postedAddition.getPrice());
                    assertEquals(group.getId(), postedAddition.getGroup().getId());
                    assertEquals(group.getName(), postedAddition.getGroup().getName());
                });
    }


    @Test
    public void testGet_shouldReturnAdditions() {
        List<Addition> additions = new ArrayList<>();
        repository.deleteAll().thenMany(repository.saveAll(Flux.fromIterable(List.of(
                new Addition(null, "addition 1", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Addition(null, "addition 2", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Addition(null, "addition 3", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group),
                new Addition(null, "addition 4", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )))).subscribe(additions::add);

        webTestClient.get()
                .uri("/api/addition/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AdditionDto.class)
                .hasSize(additions.size());
    }

    @Test
    public void testGetWithId_shouldReturnGroup() {
        Addition addition = repository.deleteAll().then(repository.save(
                new Addition(null, "addition", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();

        assert addition != null;
        webTestClient.get()
                .uri("/api/addition/{id}", addition.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionDto.class)
                .value(result -> assertEquals(converter.convertDocumentToDto(addition), result));
    }

    @Test
    public void testGetWithWrongId_should404() {
        String wrongId = "id";
        repository.deleteById(wrongId).subscribe();

        webTestClient.get()
                .uri("/api/addition/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testPut_shouldReturnChangedAddition() {
        Addition addition = repository.deleteAll().then(repository.save(
                new Addition(null, "addition", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();
        assert addition != null;
        addition.setName("new name");
        addition.setPrice(BigDecimal.TEN);

        webTestClient.put()
                .uri("/api/addition/{id}", addition.getId())
                .bodyValue(converter.convertDocumentToDto(addition))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionDto.class)
                .value(result -> {
                    assertEquals(addition.getName(), result.getName());
                    assertEquals(addition.getPrice(), result.getPrice());
                });
    }

    @Test
    public void testDelete_shouldDeleteFromBD() {
        Addition addition = repository.deleteAll().then(repository.save(
                new Addition(null, "addition", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();

        assert addition != null;
        webTestClient.delete()
                .uri("/api/addition/{id}", addition.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        StepVerifier.create(repository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testAddImageToAddition_shouldReturnWithNewImage() {
        Addition addition = repository.deleteAll().then(repository.save(
                new Addition(null, "addition", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();

        assert addition != null;
        FilePart imageFilePart = new TestImageFilePart();
        webTestClient
                .post()
                .uri("/api/addition/{id}/image", addition.getId())
                .bodyValue(imageFilePart)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AdditionDto.class)
                .value(postedAddition ->
                        assertNotEquals(imageUploadService.getDefaultImageName(),
                        postedAddition.getImageName()));
    }

    @Test
    void testDeleteImageFromAddition_shouldReturnWithNewImage() {
        Addition addition = repository.deleteAll().then(repository.save(
                new Addition(null, "addition", imageUploadService.getDefaultImageName(), BigDecimal.ONE, group)
        )).block();
        assertNotNull(addition);
        FilePart imageFilePart = new TestImageFilePart();
        AdditionDto additionDto = service.addImageToAddition(addition.getId(), Mono.just(imageFilePart)).block();
        assertNotNull(additionDto);
        assertNotEquals(imageUploadService.getDefaultImageName(), additionDto.getImageName());

        webTestClient
                .delete()
                .uri("/api/addition/{id}/image", addition.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AdditionDto.class)
                .value(postedAddition ->
                        assertEquals(imageUploadService.getDefaultImageName(),
                                postedAddition.getImageName()));
    }

}