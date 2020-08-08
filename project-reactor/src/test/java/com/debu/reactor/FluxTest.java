package com.debu.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;

@Slf4j
public class FluxTest {

    @Test
    public void testSubscribe() {
        Flux<Integer> integerFlux = Flux.range(1, 3)
                .log();
        integerFlux.subscribe();
        log.info("Assert---------------->");
        //Assert
        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    public void testSubscribe2() {
        Flux<Integer> integerFlux = Flux.range(1, 3)
                .log();
        integerFlux.subscribe(integer -> log.info("->{}", integer));
        log.info("Assert---------------->");
        //Assert
        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    public void testSubscribe3Error() {
        Flux<Integer> integerFlux = Flux.range(1, 5)
                .map(m -> {
                    if (m == 4) {
                        throw new IndexOutOfBoundsException("Index out of bound exception!");
                    }
                    return m;
                })
                .log();
        integerFlux.subscribe(integer -> log.info("->{}", integer), error -> log.info(error.getMessage()));
        log.info("Assert---------------->");
        //Assert
        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void testSubscribe4Error() {
        Flux<Integer> integerFlux = Flux.range(1, 5)
                .map(m -> {
                    if (m == 7) {
                        throw new IndexOutOfBoundsException("Index out of bound exception!");
                    }
                    return m;
                }).log();
        integerFlux.subscribe(integer -> log.info("->{}", integer), error -> log.error(error.getMessage()), () -> log.info("------------------>Done!"));
        log.info("Assert---------------->");
        //Assert
        StepVerifier.create(integerFlux)
                .expectNextSequence(Arrays.asList(1, 2, 3, 4, 5))
                .verifyComplete();
    }
}
