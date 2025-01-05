package com.ping;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PingService {

    private final ConcurrentHashMap<String, AtomicBoolean> runningPings = new ConcurrentHashMap<>();

    public Flux<String> ping(String ipAddress) {
        String command = System.getProperty("os.name").toLowerCase().contains("win")
                ? "ping -t " + ipAddress // Windows
                : "ping " + ipAddress;  // Linux/Unix/Mac

        // Add or retrieve the running state
        runningPings.putIfAbsent(ipAddress, new AtomicBoolean(true));
        AtomicBoolean isRunning = runningPings.get(ipAddress);

        return Flux.create(emitter -> {
            if (!isRunning.get()) {
                emitter.error(new IllegalStateException("Ping operation already stopped"));
                return;
            }
            try {
                Process process = new ProcessBuilder(command.split(" ")).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                new Thread(() -> {
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!isRunning.get()) { // Stop if requested
                                process.destroy();
                                break;
                            }
                            if (!line.trim().isEmpty()) {
                                emitter.next(line);
                            }
                        }
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.error(e);
                    } finally {
                        isRunning.set(false);
                        runningPings.remove(ipAddress);
                    }
                }).start();
            } catch (IOException e) {
                emitter.error(e);
            }
        });
    }

    public Mono<Boolean> stopPing(String ipAddress) {
        AtomicBoolean isRunning = runningPings.get(ipAddress);
        if (isRunning != null && isRunning.get()) {
            isRunning.set(false);
            return Mono.just(true);
        }
        return Mono.just(false);
    }
}
