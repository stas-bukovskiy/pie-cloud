package com.piecloud.order;

import com.piecloud.addition.Addition;
import com.piecloud.addition.AdditionRepository;
import com.piecloud.addition.group.AdditionGroup;
import com.piecloud.addition.group.AdditionGroupRepository;
import com.piecloud.ingredient.Ingredient;
import com.piecloud.ingredient.IngredientRepository;
import com.piecloud.ingredient.group.IngredientGroup;
import com.piecloud.ingredient.group.IngredientGroupRepository;
import com.piecloud.pie.Pie;
import com.piecloud.pie.PieRepository;
import com.piecloud.security.jwt.JwtTokenProvider;
import com.piecloud.user.User;
import com.piecloud.user.UserRepository;
import com.piecloud.user.UserService;
import com.piecloud.util.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static com.piecloud.addition.RandomAdditionUtil.randomAddition;
import static com.piecloud.addition.group.RandomAdditionGroupUtil.randomAdditionGroup;
import static com.piecloud.ingredient.RandomIngredientUtil.randomIngredient;
import static com.piecloud.ingredient.group.RandomIngredientGroupUtil.randomIngredientGroup;
import static com.piecloud.order.RandomOrderUtil.*;
import static com.piecloud.pie.PieUtil.randomPie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "1234";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AdditionRepository additionRepository;
    @Autowired
    private AdditionGroupRepository additionGroupRepository;
    @Autowired
    private PieRepository pieRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private IngredientGroupRepository ingredientGroupRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderConverter converter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Addition addition;
    private Pie pie;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll()
                .then(userRepository.save(new User(
                        null,
                        USERNAME,
                        passwordEncoder.encode(PASSWORD),
                        RandomStringUtils.randomEmail(),
                        true,
                        List.of("ADMIN", "COOK")
                ))).block();
        token = tokenProvider.createToken(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));

        orderRepository.deleteAll().block();
        IngredientGroup group = ingredientGroupRepository.deleteAll()
                .then(ingredientGroupRepository.save(randomIngredientGroup()))
                .block();

        List<Ingredient> ingredients = ingredientRepository.deleteAll()
                .thenMany(ingredientRepository.saveAll(List.of(
                        randomIngredient(group),
                        randomIngredient(group),
                        randomIngredient(group)
                ))).collectList().block();
        assert ingredients != null;
        pie = pieRepository.save(randomPie(ingredients)).block();

        AdditionGroup additionGroup = additionGroupRepository.save(randomAdditionGroup()).block();
        addition = additionRepository.save(randomAddition(additionGroup)).block();

    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll().block();
        userRepository.deleteAll().block();
        ingredientGroupRepository.deleteAll().block();
        ingredientRepository.deleteAll().block();
        pieRepository.deleteAll().block();
        additionGroupRepository.deleteAll().block();
        additionRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD)
    void getAllOrders() {
        List<Order> orders = List.of(
                randomOrder(addition, pie, pie),
                randomOrder(addition, pie, pie),
                randomOrder(addition, pie, pie)
        );
        User user = userService.getCurrentUser().block();
        assertNotNull(user);
        orders.forEach(order -> order.setUserId(user.getId()));
        orders = orderRepository.saveAll(orders).collectList().block();
        assertNotNull(orders);
        assertEquals(3, orders.size());

        webTestClient
                .get()
                .uri("/api/order/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderDto.class)
                .hasSize(orders.size());
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD)
    void postOrder() {
        Pie pieWithIngredients = new Pie();
        pieWithIngredients.setIngredients(pie.getIngredients());
        Order order = randomOrder(addition, pie, pieWithIngredients);
        OrderDto orderDtoToPost = converter.convertDocumentToDto(order);
        webTestClient
                .post()
                .uri("/api/order/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(orderDtoToPost)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderDto.class)
                .value(postedOrderDto -> {
                    assertNotNull(postedOrderDto.getId());
                    assertEquals(OrderStatus.IN_LINE, postedOrderDto.getStatus());
                    assertNotNull(postedOrderDto.getCreatedDate());
                    assertEquals(countPrice(converter.convertDtoToDocument(postedOrderDto)), postedOrderDto.getPrice());
                });
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = "ADMIN")
    void postOrderWithInvalidOrderLines() {
        OrderDto orderDtoToPost = randomOrderDto();
        webTestClient
                .post()
                .uri("/api/order/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(orderDtoToPost)
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = {"ADMIN"})
    void changeStatus() {
        Order order = randomOrder(addition, pie, pie);
        order.setId(null);
        OrderStatus status = OrderStatus.COMPLETED;
        order = orderRepository.save(order).block();
        assertNotNull(order);

        webTestClient.patch()
                .uri("/api/order/{id}/status", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", status.name()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class)
                .value(orderDto -> assertEquals(OrderStatus.COMPLETED, orderDto.getStatus()));
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = {"ADMIN"})
    void changeInvalidStatus_shouldReturn400() {
        Order order = randomOrder(addition, pie, pie);
        orderRepository.save(order).block();

        webTestClient.patch()
                .uri("/api/order/{id}/status", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "invalid"))
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD, roles = {"ADMIN"})
    void changeNullStatus_shouldReturn400() {
        Order order = randomOrder(addition, pie, pie);
        orderRepository.save(order).block();

        webTestClient.patch()
                .uri("/api/order/{id}/status", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(400);
    }
}