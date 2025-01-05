package com.ping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @PostMapping("/stop")
    public Mono<ResponseEntity<String>> stopPing(@RequestParam String ipAddress) {
        return pingService.stopPing(ipAddress)
                .map(stopped -> {
                    if (stopped) {
                        return ResponseEntity.ok().body("Ping operation for IP " + ipAddress + " has been stopped.");
                    } else {
                        return ResponseEntity.status(404).body("No active ping operation found for IP " + ipAddress);
                    }
                });
    }
}