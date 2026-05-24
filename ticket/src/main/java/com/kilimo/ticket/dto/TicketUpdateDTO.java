package com.kilimo.ticket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketUpdateDTO {
    private String status;
    private String priority;
    private Long assignedToId;
    private Boolean clearAssignment;
}
