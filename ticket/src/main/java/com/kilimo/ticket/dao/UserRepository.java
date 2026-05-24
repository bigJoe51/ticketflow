package com.kilimo.ticket.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kilimo.ticket.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // email as login
    Optional<User> findByUsername(String username); // optional if needed
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    List<User> findByDepartment_Id(Long departmentId);
    long countByDepartment_Id(Long departmentId);
    List<User> findByRole_Name(String roleName);

    @Query("SELECT COUNT(u) FROM User u")
Long countAllUsers();
}
