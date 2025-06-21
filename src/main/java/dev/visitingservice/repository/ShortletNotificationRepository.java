package dev.visitingservice.repository;

import dev.visitingservice.model.ShortletNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShortletNotificationRepository extends JpaRepository<ShortletNotification, UUID> {
    List<ShortletNotification> findByUserIdAndStatus(UUID userId, ShortletNotification.NotificationStatus status);
    long countByUserIdAndStatus(UUID userId, ShortletNotification.NotificationStatus status);
}

