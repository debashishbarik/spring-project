package com.debu.reactivespring;

import com.debu.reactivespring.document.Item;
import com.debu.reactivespring.repository.ItemReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
    Flux<Item> itemsFlux = Flux.fromIterable(Arrays.asList(Item.builder().id(null).description("LG TV").price("100").build(),
            Item.builder().id(null).description("Apple TV").price("200").build(),
            Item.builder().id(null).description("Samsung TV").price("300").build(),
            Item.builder().id(null).description("Onida TV").price("150").build(),
            Item.builder().id("5").description("Reliance TV").price("345").build()));

    @BeforeAll
    public void beforeCall() {
        itemReactiveRepository.deleteAll()
                .thenMany(itemsFlux)
                .flatMap(itemReactiveRepository::save)
                .doOnNext(item -> {
                    log.info("Item values->{}", item);
                })
                .doOnComplete(() -> {
                    log.info("All done");
                })
                .blockLast();
    }

    @Test
    public void findAllTest() {
        Flux<Item> itemFlux = itemReactiveRepository.findAll();

        StepVerifier.create(itemFlux)
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void findByIdTest() {
        Mono<Item> itemMono = itemReactiveRepository.findById("5");
        itemMono.flatMapMany()
        StepVerifier.create(itemMono)
                .expectSubscription()
                .expectNextMatches(m -> StringUtils.equalsIgnoreCase(m.getDescription(), "Reliance TV"))
                .verifyComplete();
    }

    @Test
    public void savaItemTest() {
        Mono<Item> itemMono = itemReactiveRepository.save(Item.builder().id(null).description("Honor mobile").price("123").build());
       // itemMono.subscribe(item -> log.info("Item saved:{}", item),throwable -> log.error("Error:{}",throwable.getMessage()),() -> log.info("Done!"));

        StepVerifier.create(itemMono.log("Saved"))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


}
