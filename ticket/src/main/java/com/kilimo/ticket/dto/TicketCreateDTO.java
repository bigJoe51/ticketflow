package com.kilimo.ticket.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TicketCreateDTO {

    private String title;

    private String description;

    private String priority;

    private String status;

    private Long categoryId;

    private Long departmentId;

    private Long createdById;

    private Long assignedToId;

}
