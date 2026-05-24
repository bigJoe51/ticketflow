package com.kilimo.ticket.mapper;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dto.NotificationDTO;
import com.kilimo.ticket.model.Notification;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification){

        NotificationDTO dto = new NotificationDTO();

        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }
}
