package dev.visitingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class BookingTopicConfig {
    // Enhanced Kafka topics configuration for booking events
    // Supports scaling, retention, and proper partitioning for high throughput

    @Bean
    public NewTopic bookingTopic(){
        return TopicBuilder.name("bookings")
                .build();
    }

    @Bean
    public NewTopic bookingEventsTopic(){
        return TopicBuilder.name("booking-events")
                .partitions(6) // For scaling across multiple consumers
                .replicas(1) // Adjust based on Kafka cluster setup
                .config("retention.ms", "604800000") // 7 days retention (7 * 24 * 60 * 60 * 1000)
                .config("cleanup.policy", "delete") // Delete old messages after retention
                .config("compression.type", "snappy") // Compress messages for better throughput
                .build();
    }

    @Bean
    public NewTopic bookingNotificationsTopic(){
        return TopicBuilder.name("booking-notifications")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "259200000") // 3 days retention for notifications
                .config("cleanup.policy", "delete")
                .build();
    }
}
