package com.kilimo.ticket.dto;

import java.time.LocalDateTime;

public class CommentDTO {

    private Long id;
    private String comment;
    private Long ticketId;
    private Long authorId;
    private String author;
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public CommentDTO(Long id, String comment, String author, LocalDateTime createdAt) {
        this.id = id;
        this.comment = comment;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Long getTicketId() { return ticketId; }

    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getAuthorId() { return authorId; }

    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
