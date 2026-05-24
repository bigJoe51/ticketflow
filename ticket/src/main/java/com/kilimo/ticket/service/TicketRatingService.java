package com.kilimo.ticket.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.TicketRatingRepository;
import com.kilimo.ticket.model.TicketRating;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketRatingService {

    private final TicketRatingRepository ratingRepository;
    private final AuditLogService auditLogService;

    public TicketRating rateTicket(TicketRating rating){
        if (rating == null || rating.getTicket() == null || rating.getRating() == 0) {
            throw new IllegalArgumentException("Rating, ticket, and rating value are required");
        }
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (rating.getCreatedAt() == null) {
            rating.setCreatedAt(LocalDateTime.now());
        }
        TicketRating saved = ratingRepository.save(rating);
        auditLogService.recordAction(
            "CREATE",
            "TicketRating",
            saved.getId(),
            "Rating submitted for ticket #" + (saved.getTicket() == null ? "?" : saved.getTicket().getId()),
            null,
            "rating=" + saved.getRating()
        );
        return saved;
    }

    public Double getStaffAverageRating(User staff){
        if (staff == null || staff.getId() == null) {
            throw new IllegalArgumentException("Valid staff user is required");
        }
        return ratingRepository.getAverageRatingForStaff(staff);
    }

    public List<TicketRating> getRatingsForStaff(Long staffId) {
        if (staffId == null || staffId <= 0) {
            throw new IllegalArgumentException("Valid staff ID is required");
        }
        return ratingRepository.findByStaff_IdOrderByCreatedAtDesc(staffId);
    }

    public List<TicketRating> getAllRatings() {
        return ratingRepository.findAllByOrderByCreatedAtDesc();
    }
}
