package com.academymty.webflux.mono.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Te dice que hilo te esta atendiendo.
 *
 * Llamalo diez veces seguidas: vas a ver que se repiten unos pocos nombres
 * (reactor-http-nio-1, -2, -3, -4...), no diez distintos. Ese es el event loop.
 * En el proyecto 15, con Tomcat, verias diez nombres diferentes.
 */
@RestController
public class HiloRestController {

    @GetMapping("/api/hilo")
    public Mono<Map<String, Object>> hilo() {
        return Mono.just(Map.of(
                "hilo", Thread.currentThread().getName(),
                "hilosDisponibles", Runtime.getRuntime().availableProcessors(),
                "pista", "Llama varias veces: se repiten los mismos nombres. Eso es el event loop."
        ));
    }
}
