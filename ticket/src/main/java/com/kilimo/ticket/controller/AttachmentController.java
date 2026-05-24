package com.kilimo.ticket.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kilimo.ticket.model.Attachment;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.AttachmentService;
import com.kilimo.ticket.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final UserService userService;
    private final Path attachmentStoragePath = Paths.get(System.getProperty("user.home"), "ticketflow_uploads", "attachments");

    @PostMapping("/ticket/{ticketId}")
    public ResponseEntity<?> uploadTicketAttachments(@PathVariable Long ticketId,
                                                     @RequestParam("uploadedById") Long uploadedById,
                                                     @RequestParam("files") MultipartFile[] files) {
        try {
            List<Attachment> saved = attachmentService.uploadTicketAttachments(ticketId, uploadedById, files);
            List<Map<String, Object>> response = saved.stream()
                .map(this::toAttachmentMap)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Attachment upload failed"));
        }
    }

    @PostMapping("/ticket/{ticketId}/self")
    public ResponseEntity<?> uploadTicketAttachmentsSelf(@PathVariable Long ticketId,
                                                         @RequestParam("files") MultipartFile[] files,
                                                         Authentication authentication) {
        try {
            String email = authentication == null ? null : authentication.getName();
            User uploader = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));
            List<Attachment> saved = attachmentService.uploadTicketAttachments(ticketId, uploader.getId(), files);
            List<Map<String, Object>> response = saved.stream()
                .map(this::toAttachmentMap)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Attachment upload failed"));
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public List<Map<String, Object>> getTicketAttachments(@PathVariable Long ticketId) {
        return attachmentService.getTicketAttachments(ticketId).stream()
            .map(this::toAttachmentMap)
            .collect(Collectors.toList());
    }

    @GetMapping("/file/{fileName:.+}")
    public ResponseEntity<Resource> getAttachmentFile(@PathVariable String fileName) {
        try {
            Path filePath = attachmentStoragePath.resolve(fileName).normalize();
            if (!filePath.startsWith(attachmentStoragePath) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> toAttachmentMap(Attachment attachment) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", attachment.getId());
        item.put("fileName", attachment.getFileName());
        item.put("fileUrl", attachment.getFileUrl());
        item.put("fileType", attachment.getFileType());
        item.put("fileSize", attachment.getFileSize());
        return item;
    }
}
