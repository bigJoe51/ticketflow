package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.NotificationDTO;
import com.kilimo.ticket.mapper.NotificationMapper;
import com.kilimo.ticket.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId){
        return notificationService.getUnreadNotifications(userId)
                .stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/read/{id}")
    public void markNotificationRead(@PathVariable Long id){
        notificationService.markAsRead(id);
    }
}