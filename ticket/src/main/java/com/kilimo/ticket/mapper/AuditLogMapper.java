package com.kilimo.ticket.mapper;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dto.AuditLogDTO;
import com.kilimo.ticket.model.AuditLog;

@Component
public class AuditLogMapper {

    public AuditLogDTO toDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        
        dto.setId(log.getId());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setOldValue(log.getOldValue());
        dto.setNewValue(log.getNewValue());
        dto.setTimestamp(log.getTimestamp());
        dto.setIpAddress(log.getIpAddress());
        dto.setDescription(log.getDescription());
        
        if (log.getPerformedBy() != null) {
            dto.setPerformedBy(log.getPerformedBy().getEmail());
            dto.setPerformedByUserId(log.getPerformedBy().getId());
            dto.setPerformedByUsername(log.getPerformedBy().getUsername());
        }
        
        return dto;
    }
}
