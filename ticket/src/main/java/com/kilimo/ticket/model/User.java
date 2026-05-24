package com.kilimo.ticket.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String firstName;
    private String lastName;

    @Column(unique=true)
    private String email;

    private String password;
    private String phoneNumber;
    private String profileImageUrl;
    private String status;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @OneToMany(mappedBy = "createdBy")
    private List<Ticket> createdTickets;

    @OneToMany(mappedBy = "assignedTo")
    private List<Ticket> assignedTickets;

    @OneToMany(mappedBy = "author")
    private List<TicketComment> comments;

    @OneToMany(mappedBy = "uploadedBy")
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "staff")
    private List<TicketRating> ratings;

    @OneToMany(mappedBy = "assignedTo")
    private List<Asset> assets;

    @OneToMany(mappedBy = "createdBy")
    private List<KnowledgeArticle> createdArticles;

    @OneToMany(mappedBy = "approvedBy")
    private List<KnowledgeArticle> approvedArticles;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "performedBy")
    private List<AuditLog> auditLogs;
}
