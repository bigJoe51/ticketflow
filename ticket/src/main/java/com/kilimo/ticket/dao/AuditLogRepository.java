package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPerformedBy_Id(Long userId);
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findTop500ByOrderByTimestampDesc();
}
