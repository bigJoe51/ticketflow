package com.kilimo.ticket.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.AssetRepository;
import com.kilimo.ticket.dao.AttachmentRepository;
import com.kilimo.ticket.dao.DepartmentRepository;
import com.kilimo.ticket.dao.EscalationRuleRepository;
import com.kilimo.ticket.dao.KnowledgeArticleRepository;
import com.kilimo.ticket.dao.NotificationRepository;
import com.kilimo.ticket.dao.RoleRepository;
import com.kilimo.ticket.dao.SLARepository;
import com.kilimo.ticket.dao.TicketCategoryRepository;
import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.Asset;
import com.kilimo.ticket.model.Attachment;
import com.kilimo.ticket.model.Department;
import com.kilimo.ticket.model.EscalationRule;
import com.kilimo.ticket.model.KnowledgeArticle;
import com.kilimo.ticket.model.Notification;
import com.kilimo.ticket.model.Role;
import com.kilimo.ticket.model.SLA;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.TicketCategory;
import com.kilimo.ticket.model.TicketComment;
import com.kilimo.ticket.model.TicketRating;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.AuditLogService;
import com.kilimo.ticket.service.TicketCommentService;
import com.kilimo.ticket.service.TicketRatingService;
import com.kilimo.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@Component
@Profile("exam")
@RequiredArgsConstructor
public class ExamDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final TicketRepository ticketRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final NotificationRepository notificationRepository;
    private final AttachmentRepository attachmentRepository;
    private final SLARepository slaRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final TicketService ticketService;
    private final TicketCommentService ticketCommentService;
    private final TicketRatingService ticketRatingService;
    private final AuditLogService auditLogService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = saveRole("ADMIN", "System administrator");
        Role staffRole = saveRole("STAFF", "ICT support staff");
        Role clientRole = saveRole("CLIENT", "Client end user");
        saveRole("USER", "General user");

        Department ict = saveDepartment("ICT Support", "Handles hardware, software and network support.");
        Department finance = saveDepartment("Finance", "Finance department services.");
        Department hr = saveDepartment("Human Resource", "Human resource operations.");

        TicketCategory hardware = saveCategory("Hardware", "Physical device incidents");
        TicketCategory software = saveCategory("Software", "Application and system issues");
        TicketCategory network = saveCategory("Network", "Connectivity and network services");

        EscalationRule highEscalation = saveEscalationRule("HIGH", 360, 1, "ESCALATED");
        EscalationRule mediumEscalation = saveEscalationRule("MEDIUM", 960, 1, "ESCALATED");
        EscalationRule lowEscalation = saveEscalationRule("LOW", 1920, 1, "ESCALATED");

        saveSla("HIGH", 120, 480, hardware, highEscalation);
        saveSla("MEDIUM", 240, 1440, software, mediumEscalation);
        saveSla("LOW", 480, 2880, network, lowEscalation);

        User admin = saveUser("admin", "Amina", "Admin", "admin@ticketflow.local", "Admin123!", adminRole, ict);
        User staffOne = saveUser("staff.joel", "Joel", "Otieno", "staff1@ticketflow.local", "Staff123!", staffRole, ict);
        User staffTwo = saveUser("staff.mary", "Mary", "Wanjiku", "staff2@ticketflow.local", "Staff123!", staffRole, ict);
        User clientOne = saveUser("client.grace", "Grace", "Njeri", "client1@ticketflow.local", "Client123!", clientRole, finance);
        User clientTwo = saveUser("client.brian", "Brian", "Mutiso", "client2@ticketflow.local", "Client123!", clientRole, hr);

        Asset laptop = saveAsset("Dell Latitude 5420", "Finance office laptop", "KLM-LAP-001", finance, clientOne, "ACTIVE", "Finance Office");
        Asset printer = saveAsset("HP LaserJet Pro", "Shared department printer", "KLM-PRN-002", hr, null, "ACTIVE", "HR Front Desk");
        saveAsset("Cisco Switch 2960", "Core switch for branch office", "KLM-NET-003", ict, staffOne, "ACTIVE", "Server Room");

        Ticket onboardingTicket = createTicket("New laptop setup", "Please install payroll tools and office apps on the assigned laptop.", "MEDIUM", software, finance, clientOne, staffOne);
        onboardingTicket.setAsset(laptop);
        onboardingTicket = ticketRepository.save(onboardingTicket);
        ticketService.updateTicketFields(onboardingTicket.getId(), "RESOLVED", "MEDIUM", staffOne.getId(), false);

        Ticket printerTicket = createTicket("Printer not responding", "The HR printer shows paper jam even after clearing the tray.", "LOW", hardware, hr, clientTwo, null);
        printerTicket.setAsset(printer);
        printerTicket = ticketRepository.save(printerTicket);

        Ticket wifiTicket = createTicket("Intermittent Wi-Fi outage", "Connectivity drops every few minutes in the finance office.", "HIGH", network, finance, clientOne, staffTwo);
        forceEscalatedState(wifiTicket, LocalDateTime.now().minusHours(9), LocalDateTime.now().minusHours(3));

        Ticket passwordTicket = createTicket("Password reset support", "Need assistance resetting ERP access credentials.", "MEDIUM", software, hr, clientTwo, staffOne);
        ticketService.updateTicketFields(passwordTicket.getId(), "IN_PROGRESS", "MEDIUM", staffOne.getId(), false);

        addComment(onboardingTicket, clientOne, "The machine is much faster now. Thank you.");
        addComment(onboardingTicket, staffOne, "Setup completed with antivirus and payroll tools installed.");
        addComment(wifiTicket, staffTwo, "Investigating switch logs and access point saturation.");
        addComment(passwordTicket, clientTwo, "I need access before end of day.");

        addRating(onboardingTicket, staffOne, 5, "Very quick turnaround and clear communication.");

        saveKnowledgeArticle("How to connect to office Wi-Fi", "Open Settings, choose the TicketFlow secure SSID, then sign in with your company email credentials.", "APPROVED", staffOne, admin);
        saveKnowledgeArticle("Printer troubleshooting checklist", "Check tray alignment, clear the rear cover, and restart the printer before opening a support ticket.", "PENDING", staffTwo, null);
        saveKnowledgeArticle("ERP password reset steps", "Use the self-service portal, then contact ICT if the OTP does not arrive within 5 minutes.", "APPROVED", staffTwo, admin);

        saveNotification(admin, "Exam demo data loaded successfully.");
        saveNotification(staffOne, "You have assigned demo tickets ready for review.");
        saveNotification(clientOne, "Your sample tickets are available in My Tickets.");

        saveAttachment(onboardingTicket, clientOne, "setup-checklist.txt", "Initial setup checklist for the new laptop.");
        saveAttachment(wifiTicket, staffTwo, "wifi-diagnostics.txt", "Switch and access point diagnostics snapshot.");

        auditLogService.recordActionByEmail(
            "SEED",
            "ExamData",
            1L,
            "Examiner demo data initialized",
            null,
            "users=5,tickets=4,assets=3,articles=3",
            admin.getEmail()
        );
    }

    private Role saveRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    private Department saveDepartment(String name, String description) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    private TicketCategory saveCategory(String name, String description) {
        TicketCategory category = new TicketCategory();
        category.setName(name);
        category.setDescription(description);
        return ticketCategoryRepository.save(category);
    }

    private EscalationRule saveEscalationRule(String priority, int thresholdMinutes, int level, String targetStatus) {
        EscalationRule rule = new EscalationRule();
        rule.setPriority(priority);
        rule.setThresholdMinutes(thresholdMinutes);
        rule.setEscalationLevel(level);
        rule.setTargetStatus(targetStatus);
        rule.setNotificationTemplate(priority + " escalation rule");
        rule.setActive(true);
        return escalationRuleRepository.save(rule);
    }

    private SLA saveSla(String priority, int responseMinutes, int resolutionMinutes, TicketCategory category, EscalationRule escalationRule) {
        SLA sla = new SLA();
        sla.setPriorityLevel(priority);
        sla.setResponseTimeMinutes(responseMinutes);
        sla.setResolutionTimeMinutes(resolutionMinutes);
        sla.setCategory(category);
        sla.setEscalationRule(escalationRule);
        return slaRepository.save(sla);
    }

    private User saveUser(String username,
                          String firstName,
                          String lastName,
                          String email,
                          String rawPassword,
                          Role role,
                          Department department) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(encoder.encode(rawPassword));
        user.setStatus("ACTIVE");
        user.setRole(role);
        user.setDepartment(department);
        return userRepository.save(user);
    }

    private Asset saveAsset(String name,
                            String description,
                            String serialNumber,
                            Department department,
                            User assignedTo,
                            String status,
                            String location) {
        Asset asset = new Asset();
        asset.setName(name);
        asset.setDescription(description);
        asset.setSerialNumber(serialNumber);
        asset.setDepartment(department);
        asset.setAssignedTo(assignedTo);
        asset.setStatus(status);
        asset.setLocation(location);
        asset.setProcurementDate(LocalDate.now().minusMonths(8));
        asset.setWarrantyExpiryDate(LocalDate.now().plusMonths(16));
        return assetRepository.save(asset);
    }

    private Ticket createTicket(String title,
                                String description,
                                String priority,
                                TicketCategory category,
                                Department department,
                                User createdBy,
                                User assignedTo) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");
        ticket.setCategory(category);
        ticket.setDepartment(department);
        ticket.setCreatedBy(createdBy);
        ticket.setAssignedTo(assignedTo);
        return ticketService.createTicket(ticket);
    }

    private void forceEscalatedState(Ticket ticket, LocalDateTime startAt, LocalDateTime dueAt) {
        Ticket managed = ticketRepository.findById(ticket.getId()).orElseThrow();
        managed.setCreatedAt(startAt.minusHours(2));
        managed.setEscalationStartAt(startAt);
        managed.setEscalationDueAt(dueAt);
        managed.setEscalatedAt(dueAt.plusMinutes(30));
        managed.setStatus("ESCALATED");
        managed.setSlaBreached(true);
        ticketRepository.save(managed);
    }

    private void addComment(Ticket ticket, User author, String content) {
        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setComment(content);
        ticketCommentService.addComment(comment);
    }

    private void addRating(Ticket ticket, User staff, int score, String feedback) {
        TicketRating rating = new TicketRating();
        rating.setTicket(ticket);
        rating.setStaff(staff);
        rating.setRating(score);
        rating.setFeedbackComment(feedback);
        ticketRatingService.rateTicket(rating);
    }

    private void saveKnowledgeArticle(String title, String content, String status, User author, User approver) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(title);
        article.setContent(content);
        article.setStatus(status);
        article.setCreatedBy(author);
        article.setApprovedBy(approver);
        article.setCreatedAt(LocalDateTime.now().minusDays(2));
        knowledgeArticleRepository.save(article);
    }

    private void saveNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private void saveAttachment(Ticket ticket, User uploadedBy, String fileName, String contents) throws Exception {
        Path storagePath = Path.of(System.getProperty("user.home"), "ticketflow_uploads", "attachments");
        Files.createDirectories(storagePath);
        String storedName = "ticket_" + ticket.getId() + "_seed_" + fileName;
        Path filePath = storagePath.resolve(storedName);
        Files.writeString(filePath, contents, StandardCharsets.UTF_8);

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setUploadedBy(uploadedBy);
        attachment.setFileName(fileName);
        attachment.setFileUrl("/attachments/file/" + storedName);
        attachment.setFileType("text/plain");
        attachment.setFileSize(Files.size(filePath));
        attachmentRepository.save(attachment);
    }
}
