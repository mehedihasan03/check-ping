package com.ping;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PingService {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public Flux<String> ping(String ipAddress) {
        String command = System.getProperty("os.name").toLowerCase().contains("win")
                ? "ping -t " + ipAddress  // Windows
                : "ping " + ipAddress;   // Linux/Unix/Mac

        return Flux.create(emitter -> {


//            if (isRunning.get()) {
//                emitter.error(new IllegalStateException("Ping already running"));
//                return;
//            }
//            isRunning.set(true);

            try {
                Process process = new ProcessBuilder(command.split(" ")).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        emitter.next(line);
                    }
                }
                process.waitFor();
                emitter.complete();

            } catch (IOException | InterruptedException e) {
                emitter.error(e);
            } finally {
                isRunning.set(false);
            }
        });
    }
}