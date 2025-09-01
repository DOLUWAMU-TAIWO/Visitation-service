package dev.visitingservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitingServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            // Application startup complete - ready for event-driven booking operations
            System.out.println("✅ ZenNest Visiting Service has started successfully!");
            System.out.println("📊 Event-driven booking system with Kafka integration is ready");
            System.out.println("🔄 REST API available at http://localhost:8181/api/shortlets/bookings");
            System.out.println("🚀 GraphQL endpoint available at http://localhost:8181/graphql");
        };
    }
}
