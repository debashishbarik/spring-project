package com.debu.reactivespring;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class FluxMonoTest {
    List<String> names = Arrays.asList("adam", "anna", "jack", "jenny");


    @Test
    public void fluxUsingIterable() {

        Flux<String> namesFlux = Flux.fromIterable(names)
                .log();

        StepVerifier.create(namesFlux)
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();
    }

    @Test
    public void fluxUsingArray() {

        String[] names = new String[]{"adam", "anna", "jack", "jenny"};

        Flux<String> namesFlux = Flux.fromArray(names);
        StepVerifier.create(namesFlux)
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();

    }

    @Test
    public void fluxUsingStream() {

        Flux<String> namesFlux = Flux.fromStream(names.stream());

        StepVerifier.create(namesFlux)
                .expectNext("adam", "anna", "jack", "jenny")
                .verifyComplete();

    }

    @Test
    public void monoUsingJustOrEmpty() {

        Mono<String> mono = Mono.justOrEmpty(null); //Mono.Empty();

        StepVerifier.create(mono.log())
                .verifyComplete();
    }

    @Test
    public void monoUsingSupplier() {

        Supplier<String> stringSupplier = () -> "adam";

        Mono<String> stringMono = Mono.fromSupplier(stringSupplier);

        log.info(stringSupplier.get());

        StepVerifier.create(stringMono.log())
                .expectNext("adam")
                .verifyComplete();
    }

    @Test
    public void fluxUsingRange() {

        Flux<Integer> integerFlux = Flux.range(1, 5).log();

        StepVerifier.create(integerFlux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void flatMapSequentialTest() {

        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"))
                .flatMapSequential(this::convertToList)
                .map(m -> {
                    log.info("->{}", LocalDateTime.now());
                    return m;
                }).log();

        StepVerifier.create(stringFlux)
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    public void flatMapParallelTest() {

        Flux<String> stringFlux = Flux.fromIterable(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"))
                .window(2)
                .flatMapSequential(m -> m.map(this::convertToList).subscribeOn(Schedulers.parallel()))
                .flatMap(q -> q)
                .map(m -> {
                    log.info("->{}", LocalDateTime.now());
                    return m;
                }).log("->" + LocalDateTime.now());

        StepVerifier.create(stringFlux)
                .expectSubscription()
                .expectNextCount(16)
                .verifyComplete();
    }


    private Flux<String> convertToList(String m) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Flux.fromIterable(Arrays.asList(m, "newArray"));
    }

    @Test
    public void mergeFlux() {
        //merge is parallel
        Flux<String> stringFlux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> stringFlux2 = Flux.just("D", "E", "F").delayElements(Duration.ofSeconds(1));
        ;

        Flux<String> mergeFlux = Flux.merge(stringFlux1, stringFlux2).map(m -> {
            log.info("->{}", LocalDateTime.now());
            return m;
        }).log();

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    public void concatFlux() {
        //merge is parallel
        Flux<String> stringFlux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        ;
        Flux<String> stringFlux2 = Flux.just("D", "E", "F").delayElements(Duration.ofSeconds(1));
        ;

        Flux<String> mergeFlux = Flux.concat(stringFlux1, stringFlux2).map(m -> {
            log.info("->{}", LocalDateTime.now());
            return m;
        }).log();

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    public void zipFlux() {
        //merge is parallel
        Flux<String> stringFlux1 = Flux.just("A", "B", "C").delayElements(Duration.ofSeconds(1));
        Flux<String> stringFlux2 = Flux.just("D", "E", "F").delayElements(Duration.ofSeconds(1));

        Flux<String> mergeFlux = Flux.zip(stringFlux1, stringFlux2, (t1, t2) -> {
            return t1.concat(t2);
        }).map(m -> {
            log.info("->{}", LocalDateTime.now());
            return m;
        }).log();

        StepVerifier.create(mergeFlux)
                .expectSubscription()
                .expectNextCount(3)// AD,BE,EF
                .verifyComplete();
    }
}
