package com.kilimo.ticket.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.RoleRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.Role;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public User createUser(User user){
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Assign USER role if no role is specified
        if (user.getRole() == null) {
            Optional<Role> defaultRole = roleRepository.findByNameIgnoreCase("USER");
            if (defaultRole.isPresent()) {
                user.setRole(defaultRole.get());
            }
        }
        
        // Set default status to ACTIVE if not specified
        if (user.getStatus() == null || user.getStatus().isEmpty()) {
            user.setStatus("ACTIVE");
        }
        
        User saved = userRepository.save(user);
        auditLogService.recordActionByEmail(
            "CREATE",
            "User",
            saved.getId(),
            "User created: " + buildUserLabel(saved),
            null,
            "role=" + (saved.getRole() == null ? "NONE" : saved.getRole().getName()) + ", status=" + saved.getStatus(),
            saved.getEmail()
        );
        return saved;
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User updateUserProfile(Long id, String firstName, String lastName, String email, String profileImageUrl) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (profileImageUrl != null) {
            user.setProfileImageUrl(profileImageUrl);
        }
        User saved = userRepository.save(user);
        notificationService.notifyUser(
            saved,
            "Your account has been created successfully."
        );
        notificationService.notifyUsers(
            userRepository.findByRole_Name("ADMIN"),
            "New user added: " + buildUserLabel(saved) + "."
        );
        auditLogService.recordActionByEmail(
            "UPDATE",
            "User",
            saved.getId(),
            "Profile updated for " + buildUserLabel(saved),
            null,
            "email=" + saved.getEmail(),
            saved.getEmail()
        );
        return saved;
    }

    public User updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String oldStatus = user.getStatus();
        user.setStatus(status);
        User saved = userRepository.save(user);
        if (oldStatus == null || !oldStatus.equalsIgnoreCase(saved.getStatus())) {
            notificationService.notifyUser(
                saved,
                "Your account status changed to " + saved.getStatus() + "."
            );
            notificationService.notifyUsers(
                userRepository.findByRole_Name("ADMIN"),
                "User status updated: " + buildUserLabel(saved) + " (" + oldStatus + " -> " + saved.getStatus() + ")."
            );
        }
        auditLogService.recordAction(
            "UPDATE",
            "User",
            saved.getId(),
            "User status updated for " + buildUserLabel(saved),
            oldStatus,
            saved.getStatus()
        );
        return saved;
    }

    public User updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String oldRole = user.getRole() == null ? "NONE" : user.getRole().getName();
        Role role = roleRepository.findByNameIgnoreCase(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.setRole(role);
        User saved = userRepository.save(user);
        if (!oldRole.equalsIgnoreCase(role.getName())) {
            notificationService.notifyUser(
                saved,
                "Your role was updated to " + role.getName() + "."
            );
            notificationService.notifyUsers(
                userRepository.findByRole_Name("ADMIN"),
                "User role updated: " + buildUserLabel(saved) + " (" + oldRole + " -> " + role.getName() + ")."
            );
        }
        auditLogService.recordAction(
            "UPDATE",
            "User",
            saved.getId(),
            "User role updated for " + buildUserLabel(saved),
            oldRole,
            role.getName()
        );
        return saved;
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    public User findOrCreateGoogleUser(String email, String fullName) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getRole() == null) {
                user.setRole(resolveGoogleDefaultRole());
                return userRepository.save(user);
            }
            return user;
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setStatus("ACTIVE");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        applyName(user, fullName);
        user.setRole(resolveGoogleDefaultRole());

        return userRepository.save(user);
    }

    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No account found for that email."));
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        return token;
    }

    public void resetPasswordByToken(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));
        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset link.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    private void applyName(User user, String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        if (normalized.isEmpty()) {
            user.setFirstName("TicketFlow");
            user.setLastName("User");
            return;
        }
        String[] parts = normalized.split("\\s+", 2);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");
    }

    private String buildUserLabel(User user) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail() == null ? "User#" + user.getId() : user.getEmail();
    }

    private Role resolveGoogleDefaultRole() {
        return roleRepository.findByNameIgnoreCase("CLIENT")
            .orElseGet(() -> roleRepository.findByNameIgnoreCase("USER").orElse(null));
    }
}
