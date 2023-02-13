package com.piecloud.addition;

import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupDto;
import com.piecloud.addition.group.AdditionGroupRepository;
import com.piecloud.image.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AdditionServiceTest {

    private static final BigDecimal PRICE = BigDecimal.TEN;
    @MockBean
    private AdditionRepository repository;
    @Autowired
    private AdditionService service;
    @Autowired
    private ImageUploadService imageUploadService;
    @MockBean
    private AdditionGroupRepository groupRepository;
    @Autowired
    private AdditionConverter converter;
    private AdditionGroup group;

    private String IMAGE_NAME;


    @BeforeEach
    public void setup() {
        group = new AdditionGroup("id", "name");
        IMAGE_NAME = imageUploadService.getDefaultImageName();
    }


    @Test
    void testGetAllAdditionsDto() {
        List<Addition> additionsToSave = List.of(
                new Addition("id1", "addition group 1",IMAGE_NAME, PRICE, group),
                new Addition("id2", "addition group 2",IMAGE_NAME, PRICE, group),
                new Addition("id3", "addition group 3",IMAGE_NAME, PRICE, group)
        );
        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(additionsToSave));

        Flux<AdditionDto> result = service.getAllAdditionsDto();

        StepVerifier.create(result)
                .expectNextCount(additionsToSave.size())
                .verifyComplete();
    }

    @Test
    void testGetAdditionsDto() {
        String ID = "id";
        Addition addition = new Addition(ID, "addition group", IMAGE_NAME, PRICE, group);
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
        String ID = "id";
        Addition addition = new Addition(ID, "addition group", IMAGE_NAME, PRICE, group);
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
        String ID = "id";
        Addition addition = new Addition(ID, "addition group", IMAGE_NAME, PRICE, group);
        AdditionDto additionDtoToSave = new AdditionDto(ID, "addition group", IMAGE_NAME, PRICE,
                new AdditionGroupDto(group.getId(), group.getName()));
        Mockito.when(repository.save(converter.convertDtoToDocument(additionDtoToSave))).thenReturn(Mono.just(addition));
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
        Addition addition = new Addition(ID, "addition", IMAGE_NAME, BigDecimal.ONE, group);
        Mockito.when(repository.findById(ID)).thenReturn(Mono.just(addition));
        addition.setImageName(imageName);
        Mockito.when(repository.save(addition)).thenReturn(Mono.just(addition));
        FilePart filePart = createFilePart();

        Mono<AdditionDto> result = service.addImageToAddition(ID, Mono.just(filePart));

        StepVerifier.create(result)
                .consumeNextWith(updated -> assertEquals(imageName, updated.getImageName()))
                .verifyComplete();
    }

    private FilePart createFilePart() {
        return new FilePart() {
            @Override
            public String filename() {
                return "some_image.png";
            }

            @Override
            public Mono<Void> transferTo(Path dest) {
                return Mono.empty();
            }

            @Override
            public String name() {
                return "name";
            }

            @Override
            public HttpHeaders headers() {
                return new HttpHeaders();
            }

            @Override
            public Flux<DataBuffer> content() {
                return null;
            }
        };
    }
}
