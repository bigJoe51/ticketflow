package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.ArticleReviewDTO;
import com.kilimo.ticket.dto.KnowledgeArticleDTO;
import com.kilimo.ticket.mapper.KnowledgeArticleMapper;
import com.kilimo.ticket.model.KnowledgeArticle;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.KnowledgeBaseService;
import com.kilimo.ticket.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final UserService userService;

    @PostMapping("/create")
    public KnowledgeArticleDTO createArticle(@RequestBody KnowledgeArticleDTO articleDTO){
        KnowledgeArticle article = knowledgeArticleMapper.toEntity(articleDTO);
        KnowledgeArticle saved = knowledgeBaseService.createArticle(article);
        return knowledgeArticleMapper.toDTO(saved);
    }

    @GetMapping("/approved")
    public List<KnowledgeArticleDTO> getApprovedArticles(){
        return knowledgeBaseService.getApprovedArticles()
                .stream()
                .map(knowledgeArticleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/pending")
    public List<KnowledgeArticleDTO> getPendingArticles(){
        return knowledgeBaseService.getPendingArticles()
                .stream()
                .map(knowledgeArticleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<KnowledgeArticleDTO> reviewArticle(@PathVariable Long id,
                                                             @RequestBody ArticleReviewDTO reviewDTO,
                                                             Authentication authentication) {
        User reviewer = userService.getUserByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));
        KnowledgeArticle reviewed = knowledgeBaseService.reviewArticle(id, reviewDTO.getStatus(), reviewer);
        return ResponseEntity.ok(knowledgeArticleMapper.toDTO(reviewed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        knowledgeBaseService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }
}
