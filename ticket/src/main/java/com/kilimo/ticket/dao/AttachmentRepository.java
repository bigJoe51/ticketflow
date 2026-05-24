package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTicket_Id(Long ticketId);
    void deleteByTicket_Id(Long ticketId);
}
