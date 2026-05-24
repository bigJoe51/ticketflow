package com.kilimo.ticket.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AssetResponseDTO {

    private Long id;
    
    private String name;
    
    private String description;
    
    private String serialNumber;
    
    private String status;
    
    private String location;
    
    private LocalDate procurementDate;
    
    private LocalDate warrantyExpiryDate;
    
    private String department;
    
    private String assignedTo;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
