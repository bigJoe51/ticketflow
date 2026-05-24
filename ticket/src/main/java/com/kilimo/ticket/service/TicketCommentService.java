package com.kilimo.ticket.service;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.TicketCommentRepository;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.TicketComment;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public TicketComment addComment(TicketComment comment){
        if (comment == null || comment.getComment() == null || comment.getComment().isEmpty()) {
            throw new IllegalArgumentException("Comment text is required");
        }
        if (comment.getTicket() == null || comment.getTicket().getId() == null) {
            throw new IllegalArgumentException("Valid ticket is required");
        }
        Ticket ticket = ticketRepository.findById(comment.getTicket().getId())
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        comment.setTicket(ticket);
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
        TicketComment saved = commentRepository.save(comment);

        Set<Long> excluded = new HashSet<>();
        if (saved.getAuthor() != null && saved.getAuthor().getId() != null) {
            excluded.add(saved.getAuthor().getId());
        }

        String preview = saved.getComment() == null ? "" : saved.getComment().trim();
        if (preview.length() > 60) {
            preview = preview.substring(0, 60) + "...";
        }
        String message = "New comment on ticket #" + ticket.getId() + ": " + preview;

        if (ticket.getCreatedBy() != null && ticket.getCreatedBy().getId() != null && !excluded.contains(ticket.getCreatedBy().getId())) {
            notificationService.notifyUser(ticket.getCreatedBy(), message);
        }
        if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId() != null && !excluded.contains(ticket.getAssignedTo().getId())) {
            notificationService.notifyUser(ticket.getAssignedTo(), message);
        }
        auditLogService.recordActionByEmail(
            "CREATE",
            "TicketComment",
            saved.getId(),
            "Comment added on ticket #" + ticket.getId(),
            null,
            saved.getComment(),
            saved.getAuthor() == null ? null : saved.getAuthor().getEmail()
        );
        return saved;
    }

    public List<TicketComment> getTicketComments(Long ticketId){
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("Valid ticket ID is required");
        }
        return commentRepository.findByTicket_Id(ticketId);
    }
}
