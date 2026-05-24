package com.kilimo.ticket.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;
    
    private String action;           // e.g., "CREATE", "UPDATE", "DELETE"
    private String entityType;       // e.g., "Ticket", "User", "Department"
    private Long entityId;           // ID of the affected entity
    private String oldValue;         // Previous value (for updates)
    private String newValue;         // New value (for updates)
    private String description;      // Human-readable summary
    private LocalDateTime timestamp;
    private String ipAddress;        // IP address of the requester
}
