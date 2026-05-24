package com.kilimo.ticket.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.dto.DashboardStatsDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardStatsDTO getAdminDashboardStats(){

        Long totalTickets = ticketRepository.countAllTickets();
        Long openTickets = ticketRepository.countOpenTickets();
        Long resolvedTickets = ticketRepository.countResolvedTickets();
        Long slaBreaches = ticketRepository.countSlaBreaches();
        Long totalUsers = userRepository.countAllUsers();

        return new DashboardStatsDTO(
                totalTickets,
                openTickets,
                resolvedTickets,
                slaBreaches,
                totalUsers
        );
    }
}