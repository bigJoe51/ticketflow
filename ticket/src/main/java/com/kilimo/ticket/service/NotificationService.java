package com.kilimo.ticket.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.NotificationRepository;
import com.kilimo.ticket.model.Notification;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification){
        if (notification == null || notification.getUser() == null) {
            throw new IllegalArgumentException("Notification and user are required");
        }
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(Long userId){
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }
        return notificationRepository.findTop50ByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId){
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Valid notification ID is required");
        }
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public Notification notifyUser(User user, String message) {
        if (user == null || user.getId() == null) {
            return null;
        }
        if (message == null || message.isBlank()) {
            return null;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message.trim());
        notification.setIsRead(false);
        return createNotification(notification);
    }

    public void notifyUsers(Collection<User> users, String message) {
        if (users == null || users.isEmpty()) {
            return;
        }
        users.stream()
            .filter(Objects::nonNull)
            .forEach((user) -> notifyUser(user, message));
    }
}
