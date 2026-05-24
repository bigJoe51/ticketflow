package com.kilimo.ticket.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.TicketCategory;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    Optional<TicketCategory> findByName(String name);
}