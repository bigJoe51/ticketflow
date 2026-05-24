package com.kilimo.ticket.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AuditLogDTO {

    private Long id;
    
    private String action;
    
    private String entityType;
    
    private Long entityId;
    
    private String oldValue;
    
    private String newValue;
    
    private LocalDateTime timestamp;
    
    private String ipAddress;
    
    private String performedBy;

    private Long performedByUserId;

    private String performedByUsername;

    private String description;
}
