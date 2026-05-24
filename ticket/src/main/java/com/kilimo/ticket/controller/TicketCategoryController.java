package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dao.TicketCategoryRepository;
import com.kilimo.ticket.dto.TicketCategoryDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryRepository ticketCategoryRepository;

    @GetMapping
    public List<TicketCategoryDTO> getCategories() {
        return ticketCategoryRepository.findAll().stream().map(category -> {
            TicketCategoryDTO dto = new TicketCategoryDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setDescription(category.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }
}
