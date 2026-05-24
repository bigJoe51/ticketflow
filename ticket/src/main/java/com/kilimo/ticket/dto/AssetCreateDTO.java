package com.kilimo.ticket.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AssetCreateDTO {

    private String name;
    
    private String description;
    
    private String serialNumber;
    
    private String status;
    
    private String location;
    
    private Long departmentId;
    
    private Long assignedToId;
    
    private String procurementDate;
    
    private String warrantyExpiryDate;
}
