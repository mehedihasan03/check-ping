package com.ping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PingController {

    private final PingService pingService;

    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ping(@RequestParam String ipAddress) {
        return pingService.ping(ipAddress)
                .map(line -> ServerSentEvent.builder(line)
                        .comment("Ping result")
                        .build())
                .doOnNext(line -> log.info("Ping result: {}", line));
    }
}