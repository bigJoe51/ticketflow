package com.kilimo.ticket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EscalationRuleDTO {
    private String priority;
    private Integer thresholdHours;
    private Integer escalationLevel;
    private String targetStatus;
    private Boolean active;
}
