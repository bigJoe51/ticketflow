package com.kilimo.ticket.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dao.RoleRepository;
import com.kilimo.ticket.dto.LoginResponse;
import com.kilimo.ticket.dto.UserProfileDTO;
import com.kilimo.ticket.mapper.UserMapper;
import com.kilimo.ticket.model.Role;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.AuditLogService;
import com.kilimo.ticket.service.PasswordResetEmailService;
import com.kilimo.ticket.service.UserService;
import com.kilimo.ticket.service.VerificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationService verificationService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final PasswordResetEmailService passwordResetEmailService;
    private final RoleRepository roleRepository;

    @PostMapping("/sign")
    public ResponseEntity<?> signIn(@RequestBody User user, jakarta.servlet.http.HttpServletResponse response){
        try {
            String token = verificationService.verify(user);
            User loggedInUser = userService.getUserByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User profile not found"));
            
            // Build login response with user data including role
            LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .userId(loggedInUser.getId() != null ? loggedInUser.getId().toString() : "")
                .username(loggedInUser.getUsername())
                .email(loggedInUser.getEmail())
                .firstName(loggedInUser.getFirstName())
                .lastName(loggedInUser.getLastName())
                .role(loggedInUser.getRole() != null ? loggedInUser.getRole().getName() : "USER")
                .department(loggedInUser.getDepartment() != null ? loggedInUser.getDepartment().getName() : "")
                .profilePicture(loggedInUser.getProfileImageUrl())
                .build();
            
            // Set token as HTTP-only cookie
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("ticketflow-token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(cookie);
            auditLogService.recordActionByEmail(
                "LOGIN",
                "User",
                loggedInUser.getId(),
                "Successful login for " + loggedInUser.getEmail(),
                null,
                "SUCCESS",
                loggedInUser.getEmail()
            );
            
            return ResponseEntity.ok(loginResponse);
        } catch (Exception ex) {
            String attemptedEmail = user == null ? null : user.getEmail();
            auditLogService.recordActionByEmail(
                "LOGIN",
                "User",
                null,
                "Failed login attempt for " + (attemptedEmail == null ? "unknown user" : attemptedEmail),
                null,
                "FAILED",
                attemptedEmail
            );
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@RequestBody User user){
        User saved = userService.createUser(user);
        return ResponseEntity.ok(userMapper.toDTO(saved));
    }

    @GetMapping("/roles")
    public List<Map<String, Object>> getSignupRoles() {
        Set<String> signupRoles = Set.of("ADMIN", "CLIENT", "STAFF");
        return roleRepository.findAll().stream()
            .filter(role -> role.getName() != null && signupRoles.contains(role.getName().toUpperCase()))
            .sorted((left, right) -> Integer.compare(roleOrder(left), roleOrder(right)))
            .map(role -> {
                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("id", role.getId());
                dto.put("name", role.getName());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private int roleOrder(Role role) {
        String name = role.getName() == null ? "" : role.getName().toUpperCase();
        if ("ADMIN".equals(name)) return 1;
        if ("CLIENT".equals(name)) return 2;
        if ("STAFF".equals(name)) return 3;
        return 4;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload == null ? null : payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }
        try {
            String token = userService.createPasswordResetToken(email.trim());
            String resetLink = passwordResetEmailService.buildResetLink(token);
            passwordResetEmailService.sendPasswordResetEmail(email.trim(), resetLink);
            Map<String, String> response = new LinkedHashMap<>();
            response.put("message", "If the email exists, a reset link has been sent.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Could not send reset email. Check mail settings."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload == null ? null : payload.get("token");
        String newPassword = payload == null ? null : payload.get("newPassword");
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token and new password are required."));
        }
        try {
            userService.resetPasswordByToken(token.trim(), newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully. You can now sign in."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
