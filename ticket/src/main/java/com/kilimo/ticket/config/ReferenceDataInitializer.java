package com.kilimo.ticket.config;

import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.DepartmentRepository;
import com.kilimo.ticket.dao.EscalationRuleRepository;
import com.kilimo.ticket.dao.RoleRepository;
import com.kilimo.ticket.dao.SLARepository;
import com.kilimo.ticket.dao.TicketCategoryRepository;
import com.kilimo.ticket.model.Department;
import com.kilimo.ticket.model.EscalationRule;
import com.kilimo.ticket.model.Role;
import com.kilimo.ticket.model.SLA;
import com.kilimo.ticket.model.TicketCategory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReferenceDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final SLARepository slaRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = ensureRole("ADMIN", "System administrator");
        ensureRole("STAFF", "ICT support staff");
        ensureRole("CLIENT", "Client end user");
        ensureRole("USER", "General user");

        Department ict = ensureDepartment("ICT Support", "Handles hardware, software and network support.");
        ensureDepartment("Finance", "Finance department services.");
        ensureDepartment("Human Resource", "Human resource operations.");
        ensureDepartment("Operations", "General business operations.");

        TicketCategory hardware = ensureCategory("Hardware", "Physical device incidents");
        TicketCategory software = ensureCategory("Software", "Application and system issues");
        TicketCategory network = ensureCategory("Network", "Connectivity and network services");
        ensureCategory("Account Access", "Login, password and permission requests");
        ensureCategory("Other", "General support requests");

        EscalationRule high = ensureEscalationRule("HIGH", 360, 1, "ESCALATED");
        EscalationRule medium = ensureEscalationRule("MEDIUM", 960, 1, "ESCALATED");
        EscalationRule low = ensureEscalationRule("LOW", 1920, 1, "ESCALATED");

        ensureSla("HIGH", 120, 480, hardware, high);
        ensureSla("MEDIUM", 240, 1440, software, medium);
        ensureSla("LOW", 480, 2880, network, low);

        // Keeps the compiler honest if future edits remove the admin/ICT bootstrap paths.
        if (admin.getId() == null || ict.getId() == null) {
            throw new IllegalStateException("Reference data initialization failed");
        }
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name)
            .map((role) -> updateDescription(role, description))
            .orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                role.setDescription(description);
                return roleRepository.save(role);
            });
    }

    private Department ensureDepartment(String name, String description) {
        return departmentRepository.findFirstByNameIgnoreCase(name)
            .map((department) -> updateDescription(department, description))
            .orElseGet(() -> {
                Department department = new Department();
                department.setName(name);
                department.setDescription(description);
                return departmentRepository.save(department);
            });
    }

    private TicketCategory ensureCategory(String name, String description) {
        return ticketCategoryRepository.findAll().stream()
            .filter((category) -> name.equalsIgnoreCase(category.getName()))
            .findFirst()
            .map((category) -> updateDescription(category, description))
            .orElseGet(() -> {
                TicketCategory category = new TicketCategory();
                category.setName(name);
                category.setDescription(description);
                return ticketCategoryRepository.save(category);
            });
    }

    private EscalationRule ensureEscalationRule(String priority, int thresholdMinutes, int level, String targetStatus) {
        return escalationRuleRepository.findFirstByPriority(priority)
            .map((rule) -> {
                rule.setThresholdMinutes(thresholdMinutes);
                rule.setEscalationLevel(level);
                rule.setTargetStatus(targetStatus);
                rule.setNotificationTemplate(priority + " escalation rule");
                rule.setActive(true);
                return escalationRuleRepository.save(rule);
            })
            .orElseGet(() -> {
                EscalationRule rule = new EscalationRule();
                rule.setPriority(priority);
                rule.setThresholdMinutes(thresholdMinutes);
                rule.setEscalationLevel(level);
                rule.setTargetStatus(targetStatus);
                rule.setNotificationTemplate(priority + " escalation rule");
                rule.setActive(true);
                return escalationRuleRepository.save(rule);
            });
    }

    private SLA ensureSla(String priority, int responseMinutes, int resolutionMinutes, TicketCategory category, EscalationRule escalationRule) {
        return slaRepository.findAll().stream()
            .filter((sla) -> samePriority(priority, sla.getPriorityLevel()))
            .filter((sla) -> sla.getCategory() != null && category.getId().equals(sla.getCategory().getId()))
            .findFirst()
            .map((sla) -> updateSla(sla, responseMinutes, resolutionMinutes, escalationRule))
            .orElseGet(() -> {
                SLA sla = new SLA();
                sla.setPriorityLevel(priority);
                sla.setCategory(category);
                return updateSla(sla, responseMinutes, resolutionMinutes, escalationRule);
            });
    }

    private Role updateDescription(Role role, String description) {
        role.setDescription(description);
        return roleRepository.save(role);
    }

    private Department updateDescription(Department department, String description) {
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    private TicketCategory updateDescription(TicketCategory category, String description) {
        category.setDescription(description);
        return ticketCategoryRepository.save(category);
    }

    private SLA updateSla(SLA sla, int responseMinutes, int resolutionMinutes, EscalationRule escalationRule) {
        sla.setResponseTimeMinutes(responseMinutes);
        sla.setResolutionTimeMinutes(resolutionMinutes);
        sla.setEscalationRule(escalationRule);
        return slaRepository.save(sla);
    }

    private boolean samePriority(String expected, String actual) {
        return expected.equalsIgnoreCase(actual == null ? "" : actual.toUpperCase(Locale.ROOT));
    }
}
