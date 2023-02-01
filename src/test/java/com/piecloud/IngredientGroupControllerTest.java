package com.piecloud;

import com.piecloud.ingredient.group.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@WebFluxTest(IngredientGroupController.class)
@Import({IngredientGroupConverter.class, IngredientGroupServiceImpl.class})
public class IngredientGroupControllerTest {

    private static final String GROUP_NAME = "ingredient_group";
    private static final String GROUP_ID = UUID.randomUUID().toString();
    @Autowired
    private WebTestClient webClient;
    @MockBean
    private IngredientGroupRepository repository;
    @MockBean
    private IngredientGroupConverter converter;

    @Test
//    @WithMockUser(username = TEST_EMAIL)
    public void testPostWithValidData_ShouldReturn200AndIngredientGroup() {
        IngredientGroup postedGroup = createGroup();
        IngredientGroupDto postedGroupDto = createGroupDto();

        Mockito.when(repository.save(postedGroup)).thenReturn(Mono.just(postedGroup));

        webClient
                .post()
                .uri("/api/ingredient/group/")
                .bodyValue(postedGroupDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(IngredientGroupDto.class);

        Mockito.verify(repository, times(1)).save(postedGroup);
    }

    @Test
    public void testPostWithInvalidData_ShouldReturn400() {
        IngredientGroupDto postedGroupDto = new IngredientGroupDto();
        postedGroupDto.setName("");

        webClient.post()
                .uri("/api/ingredient/group/")
                .bodyValue(postedGroupDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void testGet_shouldReturnIngredientGroupDto() {
        IngredientGroup group = createGroup();
        IngredientGroupDto groupDto = createGroupDto();

        Mockito.when(repository.findById(GROUP_ID)).thenReturn(Mono.just(group));

        webClient.get()
                .uri("/api/ingredient/group/{id}", GROUP_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientGroupDto.class)
                .isEqualTo(groupDto);
    }

    @Test
    public void testDelete_shouldReturnGroup() {
        IngredientGroup group = createGroup();
        Mono<Void> voidReturn  = Mono.empty();

        Mockito.when(repository.findById(GROUP_ID)).thenReturn(Mono.just(group));
        Mockito.when(repository.delete(group)).thenReturn(voidReturn);

        webClient.delete()
                .uri("/api/ingredient/group/{id}", GROUP_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    private IngredientGroup createGroup() {
        IngredientGroup group = new IngredientGroup();
        group.setId(GROUP_ID);
        group.setName(GROUP_NAME);

        return group;
    }

    private IngredientGroupDto createGroupDto() {
        IngredientGroupDto groupDto = new IngredientGroupDto();
        groupDto.setId(GROUP_ID);
        groupDto.setName(GROUP_NAME);

        return groupDto;
    }

}
