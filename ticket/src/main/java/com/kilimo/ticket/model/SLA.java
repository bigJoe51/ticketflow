package com.kilimo.ticket.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sla")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SLA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @OneToMany(mappedBy = "sla")
    private List<Ticket> tickets;
    
    private Integer responseTimeMinutes;   // Time to first response in minutes
    private Integer resolutionTimeMinutes; // Time to resolution in minutes
    private String priorityLevel;         // Priority level this SLA applies to
    
    @ManyToOne
    @JoinColumn(name = "escalation_rule_id")
    private EscalationRule escalationRule; // Escalation rule if SLA breached
}
