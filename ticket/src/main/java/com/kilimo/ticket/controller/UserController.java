package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import com.kilimo.ticket.dto.UserProfileDTO;
import com.kilimo.ticket.mapper.UserMapper;
import com.kilimo.ticket.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final Path avatarStoragePath = Paths.get(System.getProperty("user.home"), "ticketflow_uploads", "avatars");

    @GetMapping
    public List<UserProfileDTO> getAllUsers(){
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id){
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@PathVariable Long id, @RequestBody UserProfileDTO profileDTO) {
        return userService.getUserById(id)
            .map(existing -> {
                var updated = userService.updateUserProfile(
                    id,
                    profileDTO.getFirstName(),
                    profileDTO.getLastName(),
                    profileDTO.getEmail(),
                    profileDTO.getProfileImageUrl()
                );
                return ResponseEntity.ok(userMapper.toDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuppressWarnings("null")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
            }

            String ct = file.getContentType();
            String contentType = ct == null ? "" : ct.toLowerCase();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only image files are allowed"));
            }

            Files.createDirectories(avatarStoragePath);
            String original = file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename();
            String extension = "";
            if (original != null) {
                int idx = original.lastIndexOf('.');
                if (idx > -1) {
                    extension = original.substring(idx);
                }
            }
            String storedName = "avatar_" + id + "_" + UUID.randomUUID() + extension;
            Path target = avatarStoragePath.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/users/avatar/" + storedName;
            var updated = userService.updateUserProfile(id, null, null, null, avatarUrl);
            return ResponseEntity.ok(userMapper.toDTO(updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Avatar upload failed"));
        }
    }

    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String fileName) {
        try {
            Path filePath = avatarStoragePath.resolve(fileName).normalize();
            if (!filePath.startsWith(avatarStoragePath) || !Files.exists(filePath)) {
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
}
