package com.kilimo.ticket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlaRuleDTO {
    private String priorityLevel;
    private Integer responseTimeHours;
    private Integer resolutionTimeHours;
}
