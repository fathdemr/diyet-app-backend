package com.fatihdemir.diyetappbackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiyetAppBackendApplication {

    public static void main(String[] args) {
        Dotenv.configure().ignoreIfMissing().load()
                .entries()
                .forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        SpringApplication.run(DiyetAppBackendApplication.class, args);
    }
}