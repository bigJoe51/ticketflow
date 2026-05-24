package com.kilimo.ticket.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.KnowledgeArticleRepository;
import com.kilimo.ticket.model.KnowledgeArticle;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository articleRepository;
    private final AuditLogService auditLogService;

    public KnowledgeArticle createArticle(KnowledgeArticle article){
        if (article == null || article.getTitle() == null || article.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Article title is required");
        }
        if (article.getStatus() == null || article.getStatus().isBlank()) {
            article.setStatus("PENDING");
        }
        if (article.getCreatedAt() == null) {
            article.setCreatedAt(java.time.LocalDateTime.now());
        }
        KnowledgeArticle saved = articleRepository.save(article);
        auditLogService.recordActionByEmail(
            "CREATE",
            "KnowledgeArticle",
            saved.getId(),
            "Knowledge article submitted: " + saved.getTitle(),
            null,
            saved.getStatus(),
            saved.getCreatedBy() == null ? null : saved.getCreatedBy().getEmail()
        );
        return saved;
    }

    public List<KnowledgeArticle> getApprovedArticles(){
        return articleRepository.findByStatus("APPROVED");
    }

    public List<KnowledgeArticle> getPendingArticles(){
        return articleRepository.findByStatus("PENDING");
    }

    public KnowledgeArticle reviewArticle(Long articleId, String status, User reviewer) {
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("Valid article ID is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Review status is required");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalizedStatus)) {
            throw new IllegalArgumentException("Article status must be APPROVED or REJECTED");
        }
        KnowledgeArticle article = articleRepository.findById(articleId)
            .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        article.setStatus(normalizedStatus);
        article.setApprovedBy(reviewer);
        KnowledgeArticle saved = articleRepository.save(article);
        auditLogService.recordActionByEmail(
            "UPDATE",
            "KnowledgeArticle",
            saved.getId(),
            "Knowledge article review: " + saved.getTitle(),
            "PENDING",
            normalizedStatus,
            reviewer == null ? null : reviewer.getEmail()
        );
        return saved;
    }

    public void deleteArticle(Long articleId) {
        if (articleId == null || articleId <= 0) {
            throw new IllegalArgumentException("Valid article ID is required");
        }
        KnowledgeArticle article = articleRepository.findById(articleId)
            .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        articleRepository.delete(article);
        auditLogService.recordAction(
            "DELETE",
            "KnowledgeArticle",
            article.getId(),
            "Knowledge article deleted: " + article.getTitle(),
            article.getStatus(),
            null
        );
    }
}
