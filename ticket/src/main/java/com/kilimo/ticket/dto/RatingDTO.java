package com.kilimo.ticket.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RatingDTO {

    private Long id;
    
    private Integer rating;
    
    private String feedbackComment;

    private Long ticketId;

    private Long staffId;
    
    private String ticketTitle;
    
    private String staffName;

    private LocalDateTime createdAt;
}
