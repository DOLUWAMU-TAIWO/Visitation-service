package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletNotificationDTO;
import java.util.List;
import java.util.UUID;

public interface ShortletNotificationService {
    long getUnreadNotificationCount(UUID userId);
    void markNotificationsAsRead(List<UUID> notificationIds);
    List<ShortletNotificationDTO> getUnreadNotifications(UUID userId);
}

