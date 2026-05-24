package com.kilimo.ticket.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dao.AssetRepository;
import com.kilimo.ticket.dao.DepartmentRepository;
import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.DepartmentDTO;
import com.kilimo.ticket.model.Department;
import com.kilimo.ticket.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    @GetMapping
    public List<DepartmentDTO> getDepartments() {
        return departmentRepository.findAll().stream().map(department -> {
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId(department.getId());
            dto.setName(department.getName());
            dto.setDescription(department.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentDTO departmentDTO, Authentication authentication) {
        String name = departmentDTO.getName() == null ? "" : departmentDTO.getName().trim();
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department name is required"));
        }
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department already exists"));
        }
        Department department = new Department();
        department.setName(name);
        department.setDescription(departmentDTO.getDescription() == null ? "" : departmentDTO.getDescription().trim());
        Department saved = departmentRepository.save(department);
        DepartmentDTO response = new DepartmentDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        auditLogService.recordActionByEmail(
            "CREATE",
            "Department",
            saved.getId(),
            "Department created: " + saved.getName(),
            null,
            saved.getDescription(),
            authentication == null ? null : authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long departmentId,
                                              @RequestBody DepartmentDTO departmentDTO,
                                              Authentication authentication) {
        if (departmentId == null || departmentId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Valid department ID is required"));
        }
        Department department = departmentRepository.findById(departmentId).orElse(null);
        if (department == null) {
            return ResponseEntity.notFound().build();
        }
        String name = departmentDTO.getName() == null ? "" : departmentDTO.getName().trim();
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department name is required"));
        }
        Department existing = departmentRepository.findFirstByNameIgnoreCase(name).orElse(null);
        if (existing != null && !existing.getId().equals(departmentId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department already exists"));
        }

        String oldValue = department.getName() + " | " + (department.getDescription() == null ? "" : department.getDescription());
        department.setName(name);
        department.setDescription(departmentDTO.getDescription() == null ? "" : departmentDTO.getDescription().trim());
        Department saved = departmentRepository.save(department);

        DepartmentDTO response = new DepartmentDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        auditLogService.recordActionByEmail(
            "UPDATE",
            "Department",
            saved.getId(),
            "Department updated: " + saved.getName(),
            oldValue,
            saved.getName() + " | " + saved.getDescription(),
            authentication == null ? null : authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long departmentId, Authentication authentication) {
        if (departmentId == null || departmentId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Valid department ID is required"));
        }
        Department department = departmentRepository.findById(departmentId)
            .orElse(null);
        if (department == null) {
            return ResponseEntity.notFound().build();
        }
        long users = userRepository.countByDepartment_Id(departmentId);
        long tickets = ticketRepository.countByDepartment_Id(departmentId);
        long assets = assetRepository.countByDepartment_Id(departmentId);
        if (users > 0 || tickets > 0 || assets > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Department cannot be deleted while linked to records",
                "users", users,
                "tickets", tickets,
                "assets", assets
            ));
        }
        String deletedName = department.getName();
        departmentRepository.delete(department);
        auditLogService.recordActionByEmail(
            "DELETE",
            "Department",
            departmentId,
            "Department deleted: " + deletedName,
            deletedName,
            null,
            authentication == null ? null : authentication.getName()
        );
        return ResponseEntity.ok(Map.of("message", "Department deleted"));
    }
}
