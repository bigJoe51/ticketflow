package com.kilimo.ticket.model;

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
@Table(name = "escalation_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscalationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String priority;            // e.g., "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private Integer thresholdMinutes;   // Minutes before escalation triggers
    private Integer escalationLevel;    // Level of escalation (1, 2, 3...)
    
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;            // Who should escalation go to
    
    private String targetStatus;        // Ticket status to apply on escalation
    private String notificationTemplate; // Template for escalation notification
    private Boolean active;              // Whether this rule is active
}
