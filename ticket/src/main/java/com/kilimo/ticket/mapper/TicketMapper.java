package com.kilimo.ticket.mapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dao.DepartmentRepository;
import com.kilimo.ticket.dao.EscalationRuleRepository;
import com.kilimo.ticket.dao.SLARepository;
import com.kilimo.ticket.dao.TicketCategoryRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.TicketCreateDTO;
import com.kilimo.ticket.model.EscalationRule;
import com.kilimo.ticket.model.SLA;
import com.kilimo.ticket.dto.TicketResponseDTO;
import com.kilimo.ticket.model.Department;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.TicketCategory;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketMapper {
    private final TicketCategoryRepository ticketCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final SLARepository slaRepository;
    private final EscalationRuleRepository escalationRuleRepository;

    public TicketResponseDTO toDTO(Ticket ticket){

        TicketResponseDTO dto = new TicketResponseDTO();

        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());

        if (ticket.getCategory() != null) {
            dto.setCategory(ticket.getCategory().getName());
        }

        if(ticket.getCreatedBy() != null){
            dto.setCreatedBy(ticket.getCreatedBy().getEmail());
        }

        if(ticket.getAssignedTo() != null){
            dto.setAssignedStaff(ticket.getAssignedTo().getEmail());
            dto.setAssignedStaffId(ticket.getAssignedTo().getId());
            dto.setAssignedStaffName(displayName(ticket.getAssignedTo()));
        }

        if(ticket.getDepartment() != null){
            dto.setDepartment(ticket.getDepartment().getName());
        }

        String priority = normalizePriority(ticket.getPriority());
        LocalDateTime escalationStartAt = resolveEscalationStart(ticket);
        LocalDateTime escalationDueAt = resolveEscalationDueAt(ticket, priority, escalationStartAt);
        boolean escalationTriggered = ticket.getEscalatedAt() != null || isEscalatedByDeadline(ticket, escalationDueAt);

        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setSlaBreached(ticket.isSlaBreached() || isSlaBreached(ticket, priority));
        dto.setEscalationTriggered(escalationTriggered);
        dto.setEscalationStartAt(escalationStartAt);
        dto.setEscalationDueAt(escalationDueAt);
        dto.setEscalatedAt(ticket.getEscalatedAt());
        dto.setSecondsToEscalation(secondsToEscalation(escalationDueAt));
        dto.setTimeToEscalationLabel(formatTimeToEscalation(escalationDueAt, escalationTriggered));

        return dto;
    }


    public Ticket toEntity(TicketCreateDTO dto){

        Ticket ticket = new Ticket();

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(dto.getStatus());

        if (dto.getCategoryId() != null) {
            TicketCategory category = ticketCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            ticket.setCategory(category);
        }

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            ticket.setDepartment(department);
        }

        if (dto.getCreatedById() != null) {
            User createdBy = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("Created-by user not found"));
            ticket.setCreatedBy(createdBy);
        }

        if (dto.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned staff not found"));
            ticket.setAssignedTo(assignedTo);
        }

        return ticket;
    }

    private String displayName(User user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
            + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail();
    }

    private Long secondsToEscalation(LocalDateTime escalationDueAt) {
        if (escalationDueAt == null) {
            return null;
        }
        return Duration.between(LocalDateTime.now(), escalationDueAt).getSeconds();
    }

    private String formatTimeToEscalation(LocalDateTime escalationDueAt, boolean escalated) {
        if (escalationDueAt == null) {
            return "No escalation rule";
        }
        long seconds = Duration.between(LocalDateTime.now(), escalationDueAt).getSeconds();
        long absoluteSeconds = Math.abs(seconds);
        long hours = absoluteSeconds / 3600;
        long minutes = (absoluteSeconds % 3600) / 60;
        if (seconds >= 0) {
            return hours + "h " + minutes + "m remaining";
        }
        if (escalated) {
            return "Escalated " + hours + "h " + minutes + "m ago";
        }
        return "Due " + hours + "h " + minutes + "m ago";
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "MEDIUM";
        }
        return priority.trim().toUpperCase(Locale.ROOT);
    }

    private LocalDateTime resolveEscalationStart(Ticket ticket) {
        if (ticket.getEscalationStartAt() != null) {
            return ticket.getEscalationStartAt();
        }
        return ticket.getCreatedAt();
    }

    private LocalDateTime resolveEscalationDueAt(Ticket ticket, String priority, LocalDateTime escalationStartAt) {
        if (ticket.getEscalationDueAt() != null) {
            return ticket.getEscalationDueAt();
        }
        if (escalationStartAt == null) {
            return null;
        }
        Optional<EscalationRule> rule = escalationRuleRepository.findFirstByPriority(priority)
            .filter((candidate) -> Boolean.TRUE.equals(candidate.getActive()));
        return rule.map((candidate) -> escalationStartAt.plusMinutes(candidate.getThresholdMinutes())).orElse(null);
    }

    private boolean isEscalatedByDeadline(Ticket ticket, LocalDateTime escalationDueAt) {
        if (escalationDueAt == null || isTerminalStatus(ticket.getStatus())) {
            return false;
        }
        return LocalDateTime.now().isAfter(escalationDueAt);
    }

    private boolean isSlaBreached(Ticket ticket, String priority) {
        if (ticket.getCreatedAt() == null) {
            return false;
        }
        Optional<SLA> sla = slaRepository.findFirstByPriorityLevelOrderByIdDesc(priority);
        if (sla.isEmpty() || sla.get().getResolutionTimeMinutes() == null) {
            return false;
        }
        LocalDateTime breachAt = ticket.getCreatedAt().plusMinutes(sla.get().getResolutionTimeMinutes());
        if (isTerminalStatus(ticket.getStatus())) {
            return ticket.getResolvedAt() != null && ticket.getResolvedAt().isAfter(breachAt);
        }
        return LocalDateTime.now().isAfter(breachAt);
    }

    private boolean isTerminalStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return "RESOLVED".equals(normalized) || "CLOSED".equals(normalized) || "CANCELLED".equals(normalized);
    }
}
