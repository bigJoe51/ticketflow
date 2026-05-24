package com.kilimo.ticket.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@RequiredArgsConstructor
public class TicketResponseDTO {

    private Long id;

    private String title;

    private String description;

    private String status;

    private String priority;

    private String category;

    private String createdBy;

    private String assignedStaff;

    private Long assignedStaffId;

    private String assignedStaffName;

    private String department;

    private LocalDateTime createdAt;

    private Boolean slaBreached;

    private Boolean escalationTriggered;

    private LocalDateTime escalationStartAt;

    private LocalDateTime escalationDueAt;

    private LocalDateTime escalatedAt;

    private Long secondsToEscalation;

    private String timeToEscalationLabel;

}
