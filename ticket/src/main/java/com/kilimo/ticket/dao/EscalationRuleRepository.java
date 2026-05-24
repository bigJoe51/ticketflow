package com.kilimo.ticket.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.EscalationRule;

@Repository
public interface EscalationRuleRepository extends JpaRepository<EscalationRule, Long> {
    List<EscalationRule> findByPriority(String priority);
    Optional<EscalationRule> findFirstByPriority(String priority);
}
