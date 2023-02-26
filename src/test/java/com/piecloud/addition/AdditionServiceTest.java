package com.piecloud.addition;

import com.piecloud.TestImageFilePart;
import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AdditionServiceTest {
    @MockBean
    private AdditionRepository repository;
    @Autowired
    private AdditionService service;
    @MockBean
    private AdditionGroupRepository groupRepository;
    @Autowired
    private AdditionConverter converter;
    private AdditionGroup group;

    @BeforeEach
    public void setup() {
        group = randomAdditionGroup();
    }


    @Test
    void testGetAllAdditionsDto() {
        List<Addition> additionsToSave = List.of(
                randomAddition(group),
                randomAddition(group),
                randomAddition(group)
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(additionsToSave));

        Flux<AdditionDto> result = service.getAllAdditionsDto("name,asc");

        StepVerifier.create(result)
                .expectNextCount(additionsToSave.size())
                .verifyComplete();
    }

    @Test
    void testGetAdditionsDto() {
        Addition addition = randomAddition(group);
        String ID = addition.getId();
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(addition));

        Mono<AdditionDto> result = service.getAdditionDto(ID);

        StepVerifier.create(result)
                .consumeNextWith(additionDto ->
                        assertEquals(converter.convertDocumentToDto(addition), additionDto))
                .verifyComplete();
    }

    @Test
    void testGetAdditionsDtoWithWrongId_shouldThrowException() {
        String WRONG_ID = "id";
        Mockito.when(repository.findById(WRONG_ID)).thenReturn(Mono.empty());

        Mono<AdditionDto> result = service.getAdditionDto(WRONG_ID);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }


    @Test
    void testGetAdditions() {
        Addition addition = randomAddition(group);
        String ID = addition.getId();
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(addition));

        Mono<Addition> result = service.getAddition(ID);

        StepVerifier.create(result)
                .consumeNextWith(foundAddition ->
                        assertEquals(addition, foundAddition))
                .verifyComplete();
    }

    @Test
    void testGetAdditionsDtoWithNullId_shouldThrowException() {
        Mono<AdditionDto> result = service.getAdditionDto(null);

        StepVerifier.create(result)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void testCreateAddition() {
        Addition addition = randomAddition(group);
        AdditionDto additionDtoToSave = converter.convertDocumentToDto(addition);
        Mockito.when(repository.save(converter.convertDtoToDocument(additionDtoToSave)))
                .thenReturn(Mono.just(addition));
        Mockito.when(repository.existsByNameAndIdIsNot(addition.getName(), addition.getId()))
                .thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(groupRepository.findById(group.getId())).thenReturn(Mono.just(group));

        Mono<AdditionDto> result = service.createAddition(Mono.just(additionDtoToSave));

        StepVerifier.create(result)
                .consumeNextWith(savedAdditionDto -> assertEquals(additionDtoToSave, savedAdditionDto))
                .verifyComplete();
    }

    @Test
    void testAddImageToAddition() {
        String ID = "id";
        String imageName = "addition-" + ID + ".png";
        Addition addition = randomAddition(group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(addition));
        addition.setImageName(imageName);
        Mockito.when(repository.save(addition)).thenReturn(Mono.just(addition));
        FilePart filePart = new TestImageFilePart();

        Mono<AdditionDto> result = service.addImageToAddition(ID, Mono.just(filePart));

        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(imageName, updated.getImageName()))
                .verifyComplete();
    }
}
