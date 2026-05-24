package com.kilimo.ticket.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.AssetCreateDTO;
import com.kilimo.ticket.dto.AssetResponseDTO;
import com.kilimo.ticket.mapper.AssetMapper;
import com.kilimo.ticket.model.Asset;
import com.kilimo.ticket.service.AuditLogService;
import com.kilimo.ticket.service.AssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    @PostMapping("/register")
    public AssetResponseDTO registerAsset(@RequestBody AssetCreateDTO assetDTO){
        Asset asset = assetMapper.toEntity(assetDTO);
        Asset saved = assetService.registerAsset(asset);
        return assetMapper.toDTO(saved);
    }

    @GetMapping("/department/{departmentId}")
    public List<AssetResponseDTO> getDepartmentAssets(@PathVariable Long departmentId){
        return assetService.getAssetsByDepartment(departmentId)
                .stream()
                .map(assetMapper::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long assetId, Authentication authentication) {
        try {
            Asset deleted = assetService.deleteAsset(assetId);
            auditLogService.recordActionByEmail(
                "DELETE",
                "Asset",
                deleted.getId(),
                "Asset deleted: " + deleted.getName(),
                deleted.getSerialNumber(),
                null,
                authentication == null ? null : authentication.getName()
            );
            return ResponseEntity.ok(Map.of("message", "Asset deleted"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
