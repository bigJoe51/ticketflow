package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.KnowledgeArticle;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    List<KnowledgeArticle> findByCreatedBy_Id(Long userId);
    List<KnowledgeArticle> findByApprovedBy_Id(Long approverId);
    List<KnowledgeArticle> findByStatus(String status);
}
