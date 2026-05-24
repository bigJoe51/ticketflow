package com.kilimo.ticket.mapper;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dao.DepartmentRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.AssetCreateDTO;
import com.kilimo.ticket.dto.AssetResponseDTO;
import com.kilimo.ticket.model.Asset;
import com.kilimo.ticket.model.Department;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssetMapper {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public AssetResponseDTO toDTO(Asset asset) {
        AssetResponseDTO dto = new AssetResponseDTO();
        
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setDescription(asset.getDescription());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setStatus(asset.getStatus());
        dto.setLocation(asset.getLocation());
        dto.setProcurementDate(asset.getProcurementDate());
        dto.setWarrantyExpiryDate(asset.getWarrantyExpiryDate());
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        
        if (asset.getDepartment() != null) {
            dto.setDepartment(asset.getDepartment().getName());
        }
        
        if (asset.getAssignedTo() != null) {
            dto.setAssignedTo(asset.getAssignedTo().getEmail());
        }
        
        return dto;
    }

    public Asset toEntity(AssetCreateDTO dto) {
        Asset asset = new Asset();
        
        asset.setName(dto.getName());
        asset.setDescription(dto.getDescription());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setStatus(dto.getStatus());
        asset.setLocation(dto.getLocation());
        
        if (dto.getProcurementDate() != null) {
            asset.setProcurementDate(java.time.LocalDate.parse(dto.getProcurementDate()));
        }
        
        if (dto.getWarrantyExpiryDate() != null) {
            asset.setWarrantyExpiryDate(java.time.LocalDate.parse(dto.getWarrantyExpiryDate()));
        }

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            asset.setDepartment(department);
        }

        if (dto.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
            asset.setAssignedTo(assignedUser);
        }
        
        return asset;
    }
}
