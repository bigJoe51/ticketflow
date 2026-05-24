package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.AuditLogDTO;
import com.kilimo.ticket.mapper.AuditLogMapper;
import com.kilimo.ticket.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    @GetMapping
    public List<AuditLogDTO> getAllLogs(){
        return auditLogService.getAllLogs()
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    public List<AuditLogDTO> getUserLogs(@PathVariable Long userId){
        return auditLogService.getLogsByUser(userId)
                .stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/clear")
    public void clearOldLogs(@RequestParam(defaultValue = "200") int keepLatest) {
        auditLogService.clearOldLogsKeepingLatest(keepLatest);
    }
}
