package com.kilimo.ticket.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.dao.AssetRepository;
import com.kilimo.ticket.dao.TicketRepository;
import com.kilimo.ticket.model.Asset;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final TicketRepository ticketRepository;

    public Asset registerAsset(Asset asset){
        if (asset == null || asset.getName() == null || asset.getName().isEmpty()) {
            throw new IllegalArgumentException("Asset name is required");
        }
        return assetRepository.save(asset);
    }

    public List<Asset> getAssetsByDepartment(Long departmentId){
        if (departmentId == null || departmentId <= 0) {
            throw new IllegalArgumentException("Valid department ID is required");
        }
        return assetRepository.findByDepartment_Id(departmentId);
    }

    public List<Asset> getAssetsByUser(Long userId){
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }
        return assetRepository.findByAssignedTo_Id(userId);
    }

    public Asset deleteAsset(Long assetId) {
        if (assetId == null || assetId <= 0) {
            throw new IllegalArgumentException("Valid asset ID is required");
        }
        Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        long linkedTickets = ticketRepository.countByAsset_Id(assetId);
        if (linkedTickets > 0) {
            throw new IllegalStateException("Asset cannot be deleted while linked to tickets");
        }
        assetRepository.delete(asset);
        return asset;
    }
}
