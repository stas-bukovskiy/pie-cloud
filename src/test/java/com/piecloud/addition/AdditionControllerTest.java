package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupRepository;
import com.piecloud.image.ImageUploadService;
import com.piecloud.util.TestImageFilePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.RandomAdditionUtil.randomAdditionDto;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static org.junit.jupiter.api.Assertions.*;


@DirtiesContext
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
        group = groupRepository.deleteAll()
                .then(groupRepository.save(randomAdditionGroup()))
                .block();
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPost_shouldReturnAddition() {
        AdditionDto additionDto = randomAdditionDto(group.getId());

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
                    assertEquals(additionDto.getDescription(), postedAddition.getDescription());
                    assertEquals(imageUploadService.getDefaultImageName(), postedAddition.getImageName());
                    assertEquals(additionDto.getPrice(), postedAddition.getPrice());
                    assertEquals(group.getId(), postedAddition.getGroup().getId());
                    assertEquals(group.getName(), postedAddition.getGroup().getName());
                });
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPostWithNotUniqueName_shouldReturn400() {
        Addition savedAddition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        AdditionDto additionWithNotUniqueName = converter.convertDocumentToDto(savedAddition);

        webTestClient
                .post()
                .uri("/api/addition/")
                .bodyValue(additionWithNotUniqueName)
                .exchange()
                .expectStatus().isEqualTo(400);
    }


    @Test
    public void testGet_shouldReturnAdditions() {
        List<Addition> additions = repository.deleteAll()
                .thenMany(repository.saveAll(Flux.fromIterable(List.of(
                        randomAddition(group),
                        randomAddition(group),
                        randomAddition(group)
                )))).collectList().block();
        assertNotNull(additions);

        webTestClient.get()
                .uri("/api/addition/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AdditionDto.class)
                .hasSize(additions.size());
    }

    @Test
    public void testGetWithId_shouldReturnAddition() {
        Addition addition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();

        assertNotNull(addition);
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
        repository.deleteById(wrongId).block();

        webTestClient.get()
                .uri("/api/addition/{id}", wrongId)
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    public void testGetWithDeletedGroup_shouldReturnAdditionWithNullGroup() {
        Addition addition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        groupRepository.deleteById(group.getId()).block();

        assertNotNull(addition);
        webTestClient.get()
                .uri("/api/addition/{id}", addition.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionDto.class)
                .value(result -> assertNull(result.getGroup()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPut_shouldReturnChangedAddition() {
        Addition addition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        assertNotNull(addition);
        addition.setName("new name");
        addition.setPrice(BigDecimal.ONE);

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutWithNotUniqueName_should400() {
        Addition savedAddition1 = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        Addition savedAddition2 = repository.save(randomAddition(group)).block();
        assertNotNull(savedAddition1);
        assertNotNull(savedAddition2);
        savedAddition2.setName(savedAddition1.getName());

        webTestClient.put()
                .uri("/api/addition/{id}", savedAddition2.getId())
                .bodyValue(converter.convertDocumentToDto(savedAddition2))
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testPutWithoutChanges_shouldReturnSameAddition() {
        Addition savedAddition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        assertNotNull(savedAddition);

        webTestClient.put()
                .uri("/api/addition/{id}", savedAddition.getId())
                .bodyValue(converter.convertDocumentToDto(savedAddition))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionDto.class)
                .value(result -> assertEquals(converter.convertDocumentToDto(savedAddition), result));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDelete_shouldDeleteFromBD() {
        Addition addition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();
        assertNotNull(addition);

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddImageToAddition_shouldReturnWithNewImage() {
        Addition addition = repository.save(randomAddition(group)).block();
        assertNotNull(addition);

        FilePart imageFilePart = new TestImageFilePart();
        webTestClient
                .post()
                .uri("/api/addition/{id}/image", addition.getId())
                .bodyValue(imageFilePart)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AdditionDto.class)
                .value(additionWithImage ->
                        assertNotEquals(imageUploadService.getDefaultImageName(),
                                additionWithImage.getImageName()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteImageFromAddition_shouldReturnWithNewImage() {
        Addition addition = repository.deleteAll()
                .then(repository.save(randomAddition(group))).block();

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