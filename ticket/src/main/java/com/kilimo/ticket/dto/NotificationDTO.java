package com.kilimo.ticket.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class NotificationDTO {

    private Long id;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;

}