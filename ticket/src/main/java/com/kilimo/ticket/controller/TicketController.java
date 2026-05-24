package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.TicketCreateDTO;
import com.kilimo.ticket.dto.TicketResponseDTO;
import com.kilimo.ticket.dto.TicketUpdateDTO;
import com.kilimo.ticket.mapper.TicketMapper;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    @PostMapping("/create")
    public ResponseEntity<TicketResponseDTO> createTicket(@RequestBody TicketCreateDTO ticketDTO, Authentication authentication){
        if (!isAdmin(authentication)) {
            ticketDTO.setAssignedToId(null);
        }
        Ticket ticket = ticketMapper.toEntity(ticketDTO);
        Ticket saved = ticketService.createTicket(ticket);
        return ResponseEntity.ok(ticketMapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicket(@PathVariable Long id){
        return ticketService.getTicket(id)
                .map(ticket -> ResponseEntity.ok(ticketMapper.toDTO(ticket)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<TicketResponseDTO> getUserTickets(@PathVariable Long userId){
        return ticketService.getTicketsByUser(userId)
                .stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/staff/{staffId}")
    public List<TicketResponseDTO> getStaffTickets(@PathVariable Long staffId){
        return ticketService.getTicketsAssignedToStaff(staffId)
                .stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(@PathVariable Long id, @RequestBody TicketUpdateDTO updateDTO, Authentication authentication) {
        if (!isAdmin(authentication)) {
            updateDTO.setAssignedToId(null);
            updateDTO.setClearAssignment(false);
        }
        Ticket updated = ticketService.updateTicketFields(
            id,
            updateDTO.getStatus(),
            updateDTO.getPriority(),
            updateDTO.getAssignedToId(),
            updateDTO.getClearAssignment()
        );
        return ResponseEntity.ok(ticketMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwnTicket(@PathVariable Long id, Authentication authentication) {
        ticketService.deleteTicketForUser(id, authentication == null ? null : authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch((authority) -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
