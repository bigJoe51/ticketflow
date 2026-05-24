package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.TicketRating;
import com.kilimo.ticket.model.User;

@Repository
public interface TicketRatingRepository extends JpaRepository<TicketRating, Long> {
    @Query("SELECT AVG(r.rating) FROM TicketRating r WHERE r.staff = :staff")
    Double getAverageRatingForStaff(@Param("staff") User staff);

    List<TicketRating> findByStaff_Id(Long staffId);
    List<TicketRating> findByStaff_IdOrderByCreatedAtDesc(Long staffId);
    List<TicketRating> findAllByOrderByCreatedAtDesc();
    void deleteByTicket_Id(Long ticketId);
}
