package com.kilimo.ticket.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.SLA;

@Repository
public interface SLARepository extends JpaRepository<SLA, Long> {
    List<SLA> findByCategory_Id(Long categoryId);
    Optional<SLA> findFirstByPriorityLevelOrderByIdDesc(String priorityLevel);
}
