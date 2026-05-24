package com.kilimo.ticket.mapper;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.RatingDTO;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.TicketRating;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RatingMapper {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public RatingDTO toDTO(TicketRating rating) {
        RatingDTO dto = new RatingDTO();
        
        dto.setId(rating.getId());
        dto.setRating(rating.getRating());
        dto.setFeedbackComment(rating.getFeedbackComment());
        dto.setCreatedAt(rating.getCreatedAt());
        
        if (rating.getTicket() != null) {
            dto.setTicketId(rating.getTicket().getId());
            dto.setTicketTitle(rating.getTicket().getTitle());
        }
        
        if (rating.getStaff() != null) {
            dto.setStaffId(rating.getStaff().getId());
            dto.setStaffName(rating.getStaff().getFirstName() + " " + rating.getStaff().getLastName());
        }
        
        return dto;
    }

    public TicketRating toEntity(RatingDTO dto) {
        TicketRating rating = new TicketRating();
        
        rating.setId(dto.getId());
        rating.setRating(dto.getRating());
        rating.setFeedbackComment(dto.getFeedbackComment());
        rating.setCreatedAt(dto.getCreatedAt());

        if (dto.getTicketId() != null) {
            Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            rating.setTicket(ticket);
        }

        if (dto.getStaffId() != null) {
            User staff = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
            rating.setStaff(staff);
        }
        
        return rating;
    }
}
