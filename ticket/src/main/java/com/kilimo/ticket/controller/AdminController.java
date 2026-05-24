package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.AdminUserActionDTO;
import com.kilimo.ticket.dto.EscalationRuleDTO;
import com.kilimo.ticket.dto.SlaRuleDTO;
import com.kilimo.ticket.dto.TicketResponseDTO;
import com.kilimo.ticket.dto.UserProfileDTO;
import com.kilimo.ticket.mapper.TicketMapper;
import com.kilimo.ticket.mapper.UserMapper;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.AdminConfigurationService;
import com.kilimo.ticket.service.TicketService;
import com.kilimo.ticket.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final TicketService ticketService;
    private final UserMapper userMapper;
    private final TicketMapper ticketMapper;
    private final AdminConfigurationService adminConfigurationService;

    @GetMapping("/users")
    public List<UserProfileDTO> getAllUsers(){
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/tickets")
    public List<TicketResponseDTO> getAllTickets(){
        return ticketService.getAllTickets()
                .stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/config/sla")
    public List<SlaRuleDTO> getSlaRules() {
        return adminConfigurationService.getSlaRules();
    }

    @PutMapping("/config/sla")
    public List<SlaRuleDTO> updateSlaRules(@RequestBody List<SlaRuleDTO> rules) {
        return adminConfigurationService.saveSlaRules(rules);
    }

    @GetMapping("/config/escalation")
    public List<EscalationRuleDTO> getEscalationRules() {
        return adminConfigurationService.getEscalationRules();
    }

    @PutMapping("/config/escalation")
    public List<EscalationRuleDTO> updateEscalationRules(@RequestBody List<EscalationRuleDTO> rules) {
        return adminConfigurationService.saveEscalationRules(rules);
    }

    @PostMapping("/users")
    public ResponseEntity<UserProfileDTO> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(userMapper.toDTO(created));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserProfileDTO> updateUserStatus(@PathVariable Long id, @RequestBody AdminUserActionDTO actionDTO) {
        if (actionDTO.getStatus() == null || actionDTO.getStatus().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        User updated = userService.updateUserStatus(id, actionDTO.getStatus().toUpperCase());
        return ResponseEntity.ok(userMapper.toDTO(updated));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserProfileDTO> updateUserRole(@PathVariable Long id, @RequestBody AdminUserActionDTO actionDTO) {
        if (actionDTO.getRole() == null || actionDTO.getRole().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        User updated = userService.updateUserRole(id, actionDTO.getRole().toUpperCase());
        return ResponseEntity.ok(userMapper.toDTO(updated));
    }
}
