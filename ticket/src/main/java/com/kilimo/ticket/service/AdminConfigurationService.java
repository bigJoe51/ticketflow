package com.kilimo.ticket.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.EscalationRuleRepository;
import com.kilimo.ticket.dao.SLARepository;
import com.kilimo.ticket.dto.EscalationRuleDTO;
import com.kilimo.ticket.dto.SlaRuleDTO;
import com.kilimo.ticket.model.EscalationRule;
import com.kilimo.ticket.model.SLA;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminConfigurationService {

    private static final List<String> PRIORITIES = Arrays.asList("HIGH", "MEDIUM", "LOW");

    private final SLARepository slaRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<SlaRuleDTO> getSlaRules() {
        return PRIORITIES.stream()
            .map(priority -> slaRepository.findFirstByPriorityLevelOrderByIdDesc(priority).orElseGet(() -> defaultSla(priority)))
            .map(this::toSlaDto)
            .collect(Collectors.toList());
    }

    public List<SlaRuleDTO> saveSlaRules(List<SlaRuleDTO> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("SLA rules are required");
        }
        rules.forEach(this::validateSlaRule);
        rules.forEach((rule) -> {
            String priority = normalize(rule.getPriorityLevel());
            SLA sla = slaRepository.findFirstByPriorityLevelOrderByIdDesc(priority).orElseGet(SLA::new);
            sla.setPriorityLevel(priority);
            sla.setResponseTimeMinutes(rule.getResponseTimeHours() * 60);
            sla.setResolutionTimeMinutes(rule.getResolutionTimeHours() * 60);
            slaRepository.save(sla);
        });
        auditLogService.recordAction(
            "UPDATE",
            "SLA",
            null,
            "SLA configuration updated",
            null,
            "Updated " + rules.size() + " SLA rules"
        );
        return getSlaRules();
    }

    @Transactional(readOnly = true)
    public List<EscalationRuleDTO> getEscalationRules() {
        return PRIORITIES.stream()
            .map(priority -> escalationRuleRepository.findFirstByPriority(priority).orElseGet(() -> defaultEscalation(priority)))
            .map(this::toEscalationDto)
            .collect(Collectors.toList());
    }

    public List<EscalationRuleDTO> saveEscalationRules(List<EscalationRuleDTO> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Escalation rules are required");
        }
        rules.forEach(this::validateEscalationRule);
        rules.forEach((rule) -> {
            String priority = normalize(rule.getPriority());
            EscalationRule entity = escalationRuleRepository.findFirstByPriority(priority).orElseGet(EscalationRule::new);
            entity.setPriority(priority);
            entity.setThresholdMinutes(rule.getThresholdHours() * 60);
            entity.setEscalationLevel(rule.getEscalationLevel() == null ? 1 : rule.getEscalationLevel());
            entity.setTargetStatus(normalize(rule.getTargetStatus()));
            entity.setNotificationTemplate("Escalate " + priority + " tickets to " + entity.getTargetStatus());
            entity.setActive(rule.getActive() == null ? Boolean.TRUE : rule.getActive());
            escalationRuleRepository.save(entity);
        });
        auditLogService.recordAction(
            "UPDATE",
            "EscalationRule",
            null,
            "Escalation configuration updated",
            null,
            "Updated " + rules.size() + " escalation rules"
        );
        return getEscalationRules();
    }

    private void validateSlaRule(SlaRuleDTO rule) {
        if (rule.getPriorityLevel() == null || rule.getPriorityLevel().isBlank()) {
            throw new IllegalArgumentException("SLA priority is required");
        }
        if (rule.getResponseTimeHours() == null || rule.getResponseTimeHours() <= 0) {
            throw new IllegalArgumentException("Response time must be greater than zero");
        }
        if (rule.getResolutionTimeHours() == null || rule.getResolutionTimeHours() <= 0) {
            throw new IllegalArgumentException("Resolution time must be greater than zero");
        }
    }

    private void validateEscalationRule(EscalationRuleDTO rule) {
        if (rule.getPriority() == null || rule.getPriority().isBlank()) {
            throw new IllegalArgumentException("Escalation priority is required");
        }
        if (rule.getThresholdHours() == null || rule.getThresholdHours() <= 0) {
            throw new IllegalArgumentException("Escalation threshold must be greater than zero");
        }
        if (rule.getTargetStatus() == null || rule.getTargetStatus().isBlank()) {
            throw new IllegalArgumentException("Escalation status is required");
        }
    }

    private SlaRuleDTO toSlaDto(SLA sla) {
        SlaRuleDTO dto = new SlaRuleDTO();
        dto.setPriorityLevel(sla.getPriorityLevel());
        dto.setResponseTimeHours(Math.max(1, (sla.getResponseTimeMinutes() == null ? 0 : sla.getResponseTimeMinutes()) / 60));
        dto.setResolutionTimeHours(Math.max(1, (sla.getResolutionTimeMinutes() == null ? 0 : sla.getResolutionTimeMinutes()) / 60));
        return dto;
    }

    private EscalationRuleDTO toEscalationDto(EscalationRule rule) {
        EscalationRuleDTO dto = new EscalationRuleDTO();
        dto.setPriority(rule.getPriority());
        dto.setThresholdHours(Math.max(1, (rule.getThresholdMinutes() == null ? 0 : rule.getThresholdMinutes()) / 60));
        dto.setEscalationLevel(rule.getEscalationLevel() == null ? 1 : rule.getEscalationLevel());
        dto.setTargetStatus(rule.getTargetStatus() == null || rule.getTargetStatus().isBlank() ? "ESCALATED" : rule.getTargetStatus());
        dto.setActive(rule.getActive() == null ? Boolean.TRUE : rule.getActive());
        return dto;
    }

    private SLA defaultSla(String priority) {
        SLA sla = new SLA();
        sla.setPriorityLevel(priority);
        if ("HIGH".equals(priority)) {
            sla.setResponseTimeMinutes(120);
            sla.setResolutionTimeMinutes(480);
        } else if ("MEDIUM".equals(priority)) {
            sla.setResponseTimeMinutes(240);
            sla.setResolutionTimeMinutes(1440);
        } else {
            sla.setResponseTimeMinutes(480);
            sla.setResolutionTimeMinutes(2880);
        }
        return sla;
    }

    private EscalationRule defaultEscalation(String priority) {
        EscalationRule rule = new EscalationRule();
        rule.setPriority(priority);
        rule.setEscalationLevel(1);
        rule.setTargetStatus("ESCALATED");
        rule.setActive(Boolean.TRUE);
        if ("HIGH".equals(priority)) {
            rule.setThresholdMinutes(360);
        } else if ("MEDIUM".equals(priority)) {
            rule.setThresholdMinutes(960);
        } else {
            rule.setThresholdMinutes(1920);
        }
        return rule;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
