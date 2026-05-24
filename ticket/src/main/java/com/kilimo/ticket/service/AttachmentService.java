package com.kilimo.ticket.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kilimo.ticket.dao.AttachmentRepository;
import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.Attachment;
import com.kilimo.ticket.model.Ticket;
import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    private final Path attachmentStoragePath = Paths.get(System.getProperty("user.home"), "ticketflow_uploads", "attachments");

    public List<Attachment> uploadTicketAttachments(Long ticketId, Long uploadedById, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file is required");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        User uploader = userRepository.findById(uploadedById)
            .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));

        try {
            Files.createDirectories(attachmentStoragePath);
            List<Attachment> savedAttachments = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String originalName = sanitizeOriginalFileName(file.getOriginalFilename());
                String storedName = "ticket_" + ticketId + "_" + UUID.randomUUID() + "_" + originalName;
                Path target = attachmentStoragePath.resolve(storedName).normalize();
                if (!target.startsWith(attachmentStoragePath)) {
                    throw new IllegalArgumentException("Invalid attachment file name");
                }
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                Attachment attachment = new Attachment();
                attachment.setTicket(ticket);
                attachment.setUploadedBy(uploader);
                attachment.setFileName(originalName);
                attachment.setFileUrl("/attachments/file/" + storedName);
                attachment.setFileType(Objects.toString(file.getContentType(), "application/octet-stream"));
                attachment.setFileSize(file.getSize());
                savedAttachments.add(attachmentRepository.save(attachment));
            }
            if (savedAttachments.isEmpty()) {
                throw new IllegalArgumentException("No valid files were uploaded");
            }
            auditLogService.recordActionByEmail(
                "CREATE",
                "Attachment",
                savedAttachments.get(0).getId(),
                "Uploaded " + savedAttachments.size() + " attachment(s) to ticket #" + ticketId,
                null,
                savedAttachments.stream().map(Attachment::getFileName).reduce((a, b) -> a + ", " + b).orElse(""),
                uploader.getEmail()
            );

            return savedAttachments;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Attachment upload failed", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Attachment> getTicketAttachments(Long ticketId) {
        return attachmentRepository.findByTicket_Id(ticketId);
    }

    public void deleteTicketAttachments(Long ticketId) {
        List<Attachment> attachments = attachmentRepository.findByTicket_Id(ticketId);
        for (Attachment attachment : attachments) {
            deleteStoredFile(attachment.getFileUrl());
        }
        attachmentRepository.deleteByTicket_Id(ticketId);
    }

    private String sanitizeOriginalFileName(String fileName) {
        String source = fileName == null ? "file" : fileName;
        String normalized = source.replace("\\", "_").replace("/", "_").replaceAll("\\s+", "_");
        normalized = normalized.replaceAll("[^a-zA-Z0-9._-]", "");
        if (normalized.isBlank()) {
            return "file";
        }
        return normalized;
    }

    private void deleteStoredFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        String prefix = "/attachments/file/";
        if (!fileUrl.startsWith(prefix)) {
            return;
        }
        String fileName = fileUrl.substring(prefix.length());
        Path filePath = attachmentStoragePath.resolve(fileName).normalize();
        try {
            if (filePath.startsWith(attachmentStoragePath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (Exception ignored) {
            // Keep ticket deletion moving even if a stored file is already missing.
        }
    }
}
