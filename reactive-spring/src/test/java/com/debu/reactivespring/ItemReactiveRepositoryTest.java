package com.debu.reactivespring;

import com.debu.reactivespring.document.Item;
import com.debu.reactivespring.repository.ItemReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;

@Slf4j
@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemReactiveRepositoryTest {
    @Autowired
    ItemReactiveRepository itemReactiveRepository;
    Flux<Item> itemFlux = Flux.fromIterable(Arrays.asList(Item.builder().id(null).description("LG TV").price("100").build(),
            Item.builder().id(null).description("Apple TV").price("200").build(),
            Item.builder().id(null).description("Samsung TV").price("300").build(),
            Item.builder().id(null).description("Onida TV").price("150").build()));

    @BeforeAll
    public void beforeCall() {
        itemReactiveRepository.deleteAll()
                .thenMany(itemFlux)
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> {
                    log.info("->{}", item);
                })
                .doOnComplete(() -> {
                    log.info("All done");
                })
                .blockLast();
    }

    @Test
    public void findAllTest() {
        Flux<Item> itemFlux = itemReactiveRepository.findAll().log();

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }
}
