package com.piecloud;

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
import com.piecloud.user.User;
import com.piecloud.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Profile("dev")
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {

    private final AdditionRepository additionRepository;
    private final AdditionGroupRepository additionGroupRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final PieRepository pieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(value = ApplicationReadyEvent.class)
    public void initIngredientsAndPies() {
        log.info("start ingredient data initialization...");
        ingredientGroupRepository.deleteAll()
                .then(ingredientGroupRepository.save(new IngredientGroup(null, "веганс")))
                .subscribe(group -> ingredientRepository.deleteAll()
                        .thenMany(ingredientRepository.saveAll(List.of(
                                new Ingredient(null, "тофу", "some.png", BigDecimal.TEN, group.getId(), group),
                                new Ingredient(null, "шпинат", "some.png", BigDecimal.TEN, group.getId(), group),
                                new Ingredient(null, "фалафель", "some.png", BigDecimal.TEN, group.getId(), group)

                        )))
                        .map(Ingredient::getId)
                        .collectList()
                        .subscribe(ingredientIds -> pieRepository.deleteAll().thenMany(pieRepository.saveAll(List.of(
                                new Pie(null, "pie 1", "some.png", BigDecimal.TEN, new HashSet<>(ingredientIds), null),
                                new Pie(null, "pie 2", "some.png", BigDecimal.TEN, new HashSet<>(ingredientIds), null),
                                new Pie(null, "pie 3", "some.png", BigDecimal.TEN, new HashSet<>(ingredientIds), null)
                        ))).subscribe())
                );
    }


    @EventListener(value = ApplicationReadyEvent.class)
    public void initAdditions() {
        log.info("start additions data initialization...");
        AdditionGroup additionGroup = additionGroupRepository.deleteAll()
                .then(additionGroupRepository.save(new AdditionGroup(null, "грибочки")))
                .block();
        List<Addition> additions = additionRepository.deleteAll()
                .thenMany(additionRepository.saveAll(List.of(
                        new Addition(null, "підпеньки", "some.png", BigDecimal.TEN, additionGroup.getId(), additionGroup),
                        new Addition(null, "білий гриб", "some.png", BigDecimal.TEN, additionGroup.getId(), additionGroup),
                        new Addition(null, "пчих", "some.png", BigDecimal.TEN, additionGroup.getId(), additionGroup)
                )))
                .map(a -> {
                    log.debug("addition: " + a);
                    return a;
                })
                .collectList().block();

        additionRepository.findAll().subscribe(a -> log.debug("found addition: " + a));
    }

    @EventListener(value = ApplicationReadyEvent.class)
    public void init() {
        log.info("start user data initialization...");
        Flux<User> initUsers = this.userRepository.deleteAll()
                .thenMany(
                        Flux.just("user", "admin")
                                .flatMap(username -> {
                                    List<String> roles = "user".equals(username) ?
                                            List.of("ROLE_USER") : Arrays.asList("ROLE_USER", "ROLE_ADMIN");

                                    User user = new User(
                                            null,
                                            username,
                                            passwordEncoder.encode("password"),
                                            username + "@example.com",
                                            true,
                                            roles
                                    );
                                    return this.userRepository.save(user);
                                })
                );

        initUsers.subscribe(
                data -> log.info("data:" + data), err -> log.error("error:" + err),
                () -> log.info("done initialization...")
        );

    }


}