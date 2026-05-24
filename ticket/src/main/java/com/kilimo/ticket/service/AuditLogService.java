package com.kilimo.ticket.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.AuditLogRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.AuditLog;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLog logAction(AuditLog log){
        if (log == null || log.getAction() == null || log.getAction().isEmpty()) {
            throw new IllegalArgumentException("AuditLog and action are required");
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(java.time.LocalDateTime.now());
        }
        return auditLogRepository.save(log);
    }

    public AuditLog recordAction(String action,
                                 String entityType,
                                 Long entityId,
                                 String description,
                                 String oldValue,
                                 String newValue) {
        String actorEmail = resolveCurrentUserEmail();
        return recordActionByEmail(action, entityType, entityId, description, oldValue, newValue, actorEmail);
    }

    public AuditLog recordActionByEmail(String action,
                                        String entityType,
                                        Long entityId,
                                        String description,
                                        String oldValue,
                                        String newValue,
                                        String actorEmail) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(java.time.LocalDateTime.now());

        if (actorEmail != null && !actorEmail.isBlank()) {
            Optional<User> performer = userRepository.findByEmail(actorEmail);
            performer.ifPresent(log::setPerformedBy);
        }
        return logAction(log);
    }

    public List<AuditLog> getLogsByUser(Long userId){
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }
        return auditLogRepository.findByPerformedBy_Id(userId);
    }

    public List<AuditLog> getAllLogs(){
        return auditLogRepository.findTop500ByOrderByTimestampDesc();
    }

    public void clearOldLogsKeepingLatest(int keepCount) {
        int keep = Math.max(0, keepCount);
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        if (logs.size() <= keep) {
            return;
        }
        List<Long> idsToDelete = logs.stream()
            .skip(keep)
            .map(AuditLog::getId)
            .collect(Collectors.toList());
        auditLogRepository.deleteAllById(idsToDelete);
    }

    private String resolveCurrentUserEmail() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return null;
            }
            String name = authentication.getName();
            if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
                return null;
            }
            return name;
        } catch (Exception ignored) {
            return null;
        }
    }
}
