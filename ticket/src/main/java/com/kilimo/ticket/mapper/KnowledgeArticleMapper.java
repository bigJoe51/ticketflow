package com.kilimo.ticket.mapper;



import org.springframework.stereotype.Component;

import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.KnowledgeArticleDTO;
import com.kilimo.ticket.model.KnowledgeArticle;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeArticleMapper {
    private final UserRepository userRepository;

    public KnowledgeArticleDTO toDTO(KnowledgeArticle article){

        KnowledgeArticleDTO dto = new KnowledgeArticleDTO();

        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setStatus(article.getStatus());
        if (article.getCreatedBy() != null) {
            dto.setCreatedById(article.getCreatedBy().getId());
        }

        if(article.getCreatedBy() != null){
            dto.setAuthor(article.getCreatedBy().getEmail());
        }

        dto.setCreatedAt(article.getCreatedAt());

        return dto;
    }

    public KnowledgeArticle toEntity(KnowledgeArticleDTO dto){
        KnowledgeArticle article = new KnowledgeArticle();
        
        article.setId(dto.getId());
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setStatus(dto.getStatus());
        article.setCreatedAt(java.time.LocalDateTime.now());

        if (dto.getCreatedById() != null) {
            User author = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("Article author not found"));
            article.setCreatedBy(author);
        }
        
        return article;
    }
}
