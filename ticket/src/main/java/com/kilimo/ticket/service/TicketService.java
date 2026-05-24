package com.kilimo.ticket.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.EscalationRuleRepository;
import com.kilimo.ticket.dao.SLARepository;
import com.kilimo.ticket.dao.TicketCommentRepository;
import com.kilimo.ticket.dao.TicketRatingRepository;
import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.EscalationRule;
import com.kilimo.ticket.model.SLA;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SLARepository slaRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRatingRepository ticketRatingRepository;
    private final AttachmentService attachmentService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public Ticket createTicket(Ticket ticket){
        if (ticket == null || ticket.getTitle() == null || ticket.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Ticket title is required");
        }
        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("OPEN");
        }
        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("MEDIUM");
        }
        if (ticket.getAssignedTo() != null && "OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }
        ticket.setCreatedAt(LocalDateTime.now());
        resetEscalationTimer(ticket);
        applySlaAndEscalationState(ticket);
        Ticket saved = ticketRepository.save(ticket);

        if (saved.getCreatedBy() != null) {
            notificationService.notifyUser(
                saved.getCreatedBy(),
                "Ticket #" + saved.getId() + " created successfully."
            );
        }
        if (saved.getAssignedTo() != null) {
            notificationService.notifyUser(
                saved.getAssignedTo(),
                "You have been assigned ticket #" + saved.getId() + " (" + saved.getTitle() + ")."
            );
        }
        notificationService.notifyUsers(
            userRepository.findByRole_Name("ADMIN"),
            "New ticket #" + saved.getId() + " was created" + ticketActorSuffix(saved.getCreatedBy()) + "."
        );
        auditLogService.recordActionByEmail(
            "CREATE",
            "Ticket",
            saved.getId(),
            "Ticket created: " + saved.getTitle(),
            null,
            "status=" + saved.getStatus() + ", priority=" + saved.getPriority(),
            saved.getCreatedBy() == null ? null : saved.getCreatedBy().getEmail()
        );
        return saved;
    }

    public Optional<Ticket> getTicket(Long id){
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid ticket ID is required");
        }
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByUser(Long userId){
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }
        return ticketRepository.findByCreatedBy_Id(userId);
    }

    public List<Ticket> getTicketsAssignedToStaff(Long staffId){
        if (staffId == null || staffId <= 0) {
            throw new IllegalArgumentException("Valid staff ID is required");
        }
        return ticketRepository.findByAssignedTo_Id(staffId);
    }

    public List<Ticket> getAllTickets(){
        return ticketRepository.findAll();
    }

    public void deleteTicketForUser(Long ticketId, String actorEmail) {
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("Valid ticket ID is required");
        }
        if (actorEmail == null || actorEmail.isBlank()) {
            throw new IllegalArgumentException("Authenticated user required");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (ticket.getCreatedBy() == null || !actor.getId().equals(ticket.getCreatedBy().getId())) {
            throw new IllegalArgumentException("You can only delete your own tickets");
        }

        User creator = ticket.getCreatedBy();
        User assignee = ticket.getAssignedTo();
        String title = ticket.getTitle();
        String previousState = "status=" + ticket.getStatus() + ", priority=" + ticket.getPriority()
            + ", createdBy=" + (creator == null ? "UNKNOWN" : creator.getEmail())
            + ", assignedTo=" + (assignee == null ? "UNASSIGNED" : assignee.getEmail());

        attachmentService.deleteTicketAttachments(ticketId);
        ticketCommentRepository.deleteByTicket_Id(ticketId);
        ticketRatingRepository.deleteByTicket_Id(ticketId);
        ticketRepository.delete(ticket);

        if (assignee != null && !assignee.getId().equals(actor.getId())) {
            notificationService.notifyUser(
                assignee,
                "Ticket #" + ticketId + " (" + title + ") was deleted by the client."
            );
        }
        notificationService.notifyUsers(
            userRepository.findByRole_Name("ADMIN"),
            "Ticket #" + ticketId + " (" + title + ") was deleted by " + actorEmail + "."
        );
        auditLogService.recordActionByEmail(
            "DELETE",
            "Ticket",
            ticketId,
            "Ticket deleted: " + title,
            previousState,
            null,
            actorEmail
        );
    }

    public Ticket updateTicket(Ticket ticket){
        if (ticket == null || ticket.getId() == null) {
            throw new IllegalArgumentException("Valid ticket with ID is required");
        }
        return ticketRepository.save(ticket);
    }

    public Ticket updateTicketFields(Long id, String status, String priority, Long assignedToId, Boolean clearAssignment) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        String previousStatus = ticket.getStatus();
        String previousPriority = ticket.getPriority();
        User previousAssignee = ticket.getAssignedTo();
        boolean assignmentChanged = false;
        boolean priorityChanged = false;

        if (status != null && !status.isBlank()) {
            ticket.setStatus(status.toUpperCase());
        }
        if (priority != null && !priority.isBlank()) {
            String normalizedPriority = priority.toUpperCase(Locale.ROOT);
            priorityChanged = previousPriority == null || !previousPriority.equalsIgnoreCase(normalizedPriority);
            ticket.setPriority(normalizedPriority);
        }
        if (assignedToId != null) {
            User assignedUser = userRepository.findById(assignedToId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned staff not found"));
            assignmentChanged = previousAssignee == null || !assignedUser.getId().equals(previousAssignee.getId());
            ticket.setAssignedTo(assignedUser);
        } else if (Boolean.TRUE.equals(clearAssignment)) {
            assignmentChanged = previousAssignee != null;
            ticket.setAssignedTo(null);
        }
        if (assignmentChanged) {
            resetEscalationTimer(ticket);
            if (ticket.getAssignedTo() != null && ("OPEN".equalsIgnoreCase(ticket.getStatus()) || "ESCALATED".equalsIgnoreCase(ticket.getStatus()))) {
                ticket.setStatus("IN_PROGRESS");
            } else if (ticket.getAssignedTo() == null && "IN_PROGRESS".equalsIgnoreCase(ticket.getStatus())) {
                ticket.setStatus("OPEN");
            }
        } else if (priorityChanged) {
            resetEscalationTimer(ticket);
        }
        if ("RESOLVED".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (ticket.getResolvedAt() != null) {
            ticket.setResolvedAt(null);
        }
        applySlaAndEscalationState(ticket);
        Ticket saved = ticketRepository.save(ticket);

        if (previousAssignee != null && (saved.getAssignedTo() == null || !previousAssignee.getId().equals(saved.getAssignedTo().getId()))) {
            notificationService.notifyUser(
                previousAssignee,
                "You were unassigned from ticket #" + saved.getId() + "."
            );
        }
        if (saved.getAssignedTo() != null && (previousAssignee == null || !saved.getAssignedTo().getId().equals(previousAssignee.getId()))) {
            notificationService.notifyUser(
                saved.getAssignedTo(),
                "You have been assigned ticket #" + saved.getId() + " (" + saved.getTitle() + ")."
            );
            if (saved.getCreatedBy() != null) {
                notificationService.notifyUser(
                    saved.getCreatedBy(),
                    "Your ticket #" + saved.getId() + " is now assigned to " + nameOf(saved.getAssignedTo()) + "."
                );
            }
        }

        if (previousStatus != null && !previousStatus.equalsIgnoreCase(saved.getStatus())) {
            String message = "Ticket #" + saved.getId() + " status changed from " + previousStatus + " to " + saved.getStatus() + ".";
            notifyTicketStakeholders(saved, message);
        }

        if (previousPriority != null && !previousPriority.equalsIgnoreCase(saved.getPriority())) {
            String message = "Ticket #" + saved.getId() + " priority changed from " + previousPriority + " to " + saved.getPriority() + ".";
            notifyTicketStakeholders(saved, message);
        }
        String assignmentSummary = saved.getAssignedTo() == null ? "UNASSIGNED" : nameOf(saved.getAssignedTo());
        auditLogService.recordAction(
            "UPDATE",
            "Ticket",
            saved.getId(),
            "Ticket updated: #" + saved.getId(),
            "status=" + previousStatus + ", priority=" + previousPriority + ", assignee=" + (previousAssignee == null ? "UNASSIGNED" : nameOf(previousAssignee)),
            "status=" + saved.getStatus() + ", priority=" + saved.getPriority() + ", assignee=" + assignmentSummary
        );

        return saved;
    }

    private boolean applySlaAndEscalationState(Ticket ticket) {
        boolean changed = false;
        String priority = normalizePriority(ticket.getPriority());
        if (!Objects.equals(priority, ticket.getPriority())) {
            ticket.setPriority(priority);
            changed = true;
        }

        SLA sla = slaRepository.findFirstByPriorityLevelOrderByIdDesc(priority).orElse(null);
        if (sla != null && (ticket.getSla() == null || !Objects.equals(ticket.getSla().getId(), sla.getId()))) {
            ticket.setSla(sla);
            changed = true;
        }

        if (ticket.getEscalationStartAt() == null) {
            ticket.setEscalationStartAt(ticket.getCreatedAt() == null ? LocalDateTime.now() : ticket.getCreatedAt());
            changed = true;
        }

        EscalationRule rule = escalationRuleRepository.findFirstByPriority(priority)
            .filter((candidate) -> Boolean.TRUE.equals(candidate.getActive()))
            .orElse(null);

        if (rule != null && ticket.getEscalationStartAt() != null) {
            LocalDateTime dueAt = ticket.getEscalationStartAt().plusMinutes(rule.getThresholdMinutes());
            if (!Objects.equals(ticket.getEscalationDueAt(), dueAt)) {
                ticket.setEscalationDueAt(dueAt);
                changed = true;
            }
        } else if (ticket.getEscalationDueAt() != null) {
            ticket.setEscalationDueAt(null);
            changed = true;
        }

        boolean slaBreached = isSlaBreached(ticket, sla);
        if (ticket.isSlaBreached() != slaBreached) {
            ticket.setSlaBreached(slaBreached);
            changed = true;
        }

        if (shouldEscalate(ticket, rule)) {
            if (ticket.getEscalatedAt() == null) {
                ticket.setEscalatedAt(LocalDateTime.now());
                changed = true;
                if (rule.getTargetStatus() != null && !rule.getTargetStatus().isBlank()
                        && !rule.getTargetStatus().equalsIgnoreCase(ticket.getStatus())) {
                    ticket.setStatus(rule.getTargetStatus().trim().toUpperCase(Locale.ROOT));
                }
                notifyEscalationTriggered(ticket, rule);
            }
        } else if (ticket.getEscalatedAt() != null && isTerminalStatus(ticket.getStatus())) {
            ticket.setEscalatedAt(null);
            changed = true;
        }

        return changed;
    }

    private boolean shouldEscalate(Ticket ticket, EscalationRule rule) {
        return rule != null
            && ticket.getEscalationDueAt() != null
            && !isTerminalStatus(ticket.getStatus())
            && LocalDateTime.now().isAfter(ticket.getEscalationDueAt());
    }

    private boolean isSlaBreached(Ticket ticket, SLA sla) {
        if (sla == null || sla.getResolutionTimeMinutes() == null || ticket.getCreatedAt() == null) {
            return false;
        }
        if (!isTerminalStatus(ticket.getStatus())) {
            return LocalDateTime.now().isAfter(ticket.getCreatedAt().plusMinutes(sla.getResolutionTimeMinutes()));
        }
        if (ticket.getResolvedAt() == null) {
            return false;
        }
        return ticket.getResolvedAt().isAfter(ticket.getCreatedAt().plusMinutes(sla.getResolutionTimeMinutes()));
    }

    private void resetEscalationTimer(Ticket ticket) {
        LocalDateTime resetAt = LocalDateTime.now();
        ticket.setEscalationStartAt(resetAt);
        ticket.setEscalationDueAt(null);
        ticket.setEscalatedAt(null);
        ticket.setSlaBreached(false);
    }

    private void notifyEscalationTriggered(Ticket ticket, EscalationRule rule) {
        String assignee = ticket.getAssignedTo() == null ? "Unassigned" : nameOf(ticket.getAssignedTo());
        String message = "Ticket #" + ticket.getId() + " has reached escalation for " + normalizePriority(ticket.getPriority())
            + " priority and needs reassignment. Current owner: " + assignee + ".";
        if (ticket.getAssignedTo() != null) {
            notificationService.notifyUser(ticket.getAssignedTo(), message);
        }
        if (ticket.getCreatedBy() != null) {
            notificationService.notifyUser(ticket.getCreatedBy(), "Your ticket #" + ticket.getId() + " has been escalated.");
        }
        notificationService.notifyUsers(userRepository.findByRole_Name("ADMIN"), message);
        auditLogService.recordAction(
            "ESCALATE",
            "Ticket",
            ticket.getId(),
            "Ticket escalated by rule",
            "status=" + ticket.getStatus(),
            "priority=" + normalizePriority(ticket.getPriority()) + ", level=" + (rule.getEscalationLevel() == null ? 1 : rule.getEscalationLevel())
        );
    }

    private boolean isTerminalStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return "RESOLVED".equals(normalized) || "CLOSED".equals(normalized) || "CANCELLED".equals(normalized);
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "MEDIUM";
        }
        return priority.trim().toUpperCase(Locale.ROOT);
    }

    private void notifyTicketStakeholders(Ticket ticket, String message) {
        Set<Long> notifiedIds = new HashSet<>();
        if (ticket.getCreatedBy() != null && ticket.getCreatedBy().getId() != null) {
            notificationService.notifyUser(ticket.getCreatedBy(), message);
            notifiedIds.add(ticket.getCreatedBy().getId());
        }
        if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId() != null && !notifiedIds.contains(ticket.getAssignedTo().getId())) {
            notificationService.notifyUser(ticket.getAssignedTo(), message);
        }
    }

    private String nameOf(User user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return user.getEmail() == null ? "staff" : user.getEmail();
    }

    private String ticketActorSuffix(User actor) {
        if (actor == null) {
            return "";
        }
        return " by " + (actor.getEmail() == null ? "a user" : actor.getEmail());
    }
}
