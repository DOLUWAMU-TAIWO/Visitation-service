package dev.visitingservice.service.impl;

import dev.visitingservice.dto.ShortletNotificationDTO;
import dev.visitingservice.model.ShortletNotification;
import dev.visitingservice.repository.ShortletNotificationRepository;
import dev.visitingservice.service.ShortletNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShortletNotificationServiceImpl implements ShortletNotificationService {

    private final ShortletNotificationRepository notificationRepository;

    @Autowired
    public ShortletNotificationServiceImpl(ShortletNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public long getUnreadNotificationCount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return notificationRepository.countByUserIdAndStatus(userId, ShortletNotification.NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(List<UUID> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new IllegalArgumentException("notificationIds cannot be null or empty");
        }
        List<ShortletNotification> notifications = notificationRepository.findAllById(notificationIds);
        if (notifications.isEmpty()) {
            throw new IllegalArgumentException("No notifications found for the provided IDs");
        }
        for (ShortletNotification notification : notifications) {
            if (notification.getStatus() == ShortletNotification.NotificationStatus.UNREAD) {
                notification.setStatus(ShortletNotification.NotificationStatus.READ);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    @Override
    public List<ShortletNotificationDTO> getUnreadNotifications(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return notificationRepository.findByUserIdAndStatus(userId, ShortletNotification.NotificationStatus.UNREAD)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ShortletNotificationDTO toDTO(ShortletNotification notification) {
        ShortletNotificationDTO dto = new ShortletNotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setStatus(notification.getStatus().name());
        return dto;
    }
}
