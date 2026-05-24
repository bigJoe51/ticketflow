package com.kilimo.ticket.dto;



import java.time.LocalDateTime;

public class KnowledgeArticleDTO {

    private Long id;
    private String title;
    private String content;
    private String status;
    private Long createdById;
    private String author;
    private LocalDateTime createdAt;

    public KnowledgeArticleDTO() {}

    public KnowledgeArticleDTO(Long id, String title, String content,
                               String status, String author,
                               LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Long getCreatedById() { return createdById; }

    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
