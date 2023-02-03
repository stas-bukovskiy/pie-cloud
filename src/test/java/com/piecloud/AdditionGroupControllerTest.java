package com.piecloud;

import com.piecloud.addition.group.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(AdditionGroupController.class)
@Import({AdditionGroupConverter.class, AdditionGroupServiceImpl.class})
public class AdditionGroupControllerTest {

    private static final String GROUP_NAME = "addition_group";
    private static final String GROUP_ID = "addition_group_id";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AdditionGroupRepository repository;
    @MockBean
    private AdditionGroupConverter converter;

    @Test
    public void testPost_shouldReturnAdditionGroup() {
        AdditionGroupDto groupDto = createGroupDto();
        AdditionGroup group = createGroup();

        Mockito.when(repository.save(group)).thenReturn(Mono.just(group));
        Mockito.when(converter.convertDocumentToDto(group)).thenReturn(groupDto);
        Mockito.when(converter.convertDtoToDocument(groupDto)).thenReturn(group);

        webTestClient
                .post()
                .uri("/api/addition/group/")
                .bodyValue(groupDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AdditionGroupDto.class);
    }

    @Test
    public void testGet_shouldReturnCreatedGroups() {
        AdditionGroupDto groupDto = createGroupDto();
        AdditionGroup group = createGroup();

        Mockito.when(repository.findById(GROUP_ID)).thenReturn(Mono.just(group));
        Mockito.when(converter.convertDocumentToDto(group)).thenReturn(groupDto);

        webTestClient.get()
                .uri("/api/addition/group/{id}", GROUP_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdditionGroupDto.class);
    }


    private AdditionGroup createGroup() {
        AdditionGroup group = new AdditionGroup();
        group.setId(GROUP_ID);
        group.setName(GROUP_NAME);
        return group;
    }

    private AdditionGroupDto createGroupDto() {
        AdditionGroupDto groupDto = new AdditionGroupDto();
        groupDto.setId(GROUP_ID);
        groupDto.setName(GROUP_NAME);
        return groupDto;
    }

}
