package com.kilimo.ticket.mapper;


import org.springframework.stereotype.Component;

import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.CommentDTO;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.TicketComment;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentDTO toDTO(TicketComment comment){

        CommentDTO dto = new CommentDTO();

        dto.setId(comment.getId());
        dto.setComment(comment.getComment());
        if (comment.getTicket() != null) {
            dto.setTicketId(comment.getTicket().getId());
        }
        if (comment.getAuthor() != null) {
            dto.setAuthorId(comment.getAuthor().getId());
        }

        if(comment.getAuthor() != null){
            dto.setAuthor(comment.getAuthor().getEmail());
        }

        dto.setCreatedAt(comment.getCreatedAt());

        return dto;
    }

    public TicketComment toEntity(CommentDTO dto){
        TicketComment comment = new TicketComment();
        
        comment.setId(dto.getId());
        comment.setComment(dto.getComment());
        comment.setCreatedAt(java.time.LocalDateTime.now());

        if (dto.getTicketId() != null) {
            Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            comment.setTicket(ticket);
        }

        if (dto.getAuthorId() != null) {
            User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
            comment.setAuthor(author);
        }
        
        return comment;
    }
}
