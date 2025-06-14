package dev.visitingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitingServiceApplication.class, args);
    }

}
