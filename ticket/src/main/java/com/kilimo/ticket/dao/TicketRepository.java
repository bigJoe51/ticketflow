package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy_Id(Long userId);
    List<Ticket> findByAssignedTo_Id(Long staffId);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByDepartment_Id(Long departmentId);
    long countByDepartment_Id(Long departmentId);
    long countByAsset_Id(Long assetId);
    List<Ticket> findByPriority(String priority);

    @Query("SELECT COUNT(t) FROM Ticket t")
Long countAllTickets();

@Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'OPEN'")
Long countOpenTickets();

@Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED'")
Long countResolvedTickets();

@Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaBreached = true")
Long countSlaBreaches();
}
