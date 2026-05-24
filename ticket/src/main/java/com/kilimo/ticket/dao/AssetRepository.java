package com.kilimo.ticket.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.Asset;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedTo_Id(Long userId);
    List<Asset> findByDepartment_Id(Long departmentId);
    long countByDepartment_Id(Long departmentId);
}
