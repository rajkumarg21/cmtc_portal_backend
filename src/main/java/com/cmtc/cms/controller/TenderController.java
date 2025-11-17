package com.cmtc.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cmtc.cms.dto.tender.TenderRequest;
import com.cmtc.cms.dto.tender.TenderResponse;
import com.cmtc.cms.model.enums.ContentStatus;
import com.cmtc.cms.service.TenderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tenders")
public class TenderController {

    @Autowired
    private TenderService tenderService; // Using the interface

    @Autowired
    private ObjectMapper objectMapper; // For parsing JSON part of multipart request

    // Public endpoint to get all published tenders
    @GetMapping("/public")
    public ResponseEntity<List<TenderResponse>> getPublishedTenders() {
        List<TenderResponse> tenders = tenderService.getPublishedTenders();
        System.out.println("HIIIIIII");
        System.out.println("SIZE: " + tenders.size());
        return ResponseEntity.ok(tenders);
    }

    // Public endpoint to get a specific tender by ID (if published)
    @GetMapping("/public/{id}")
    public ResponseEntity<TenderResponse> getPublishedtenderById(@PathVariable Long id) {
        TenderResponse tender = tenderService.getTenderById(id); // Service should handle status check for public access
        if (tender.getStatus() != ContentStatus.PUBLISHED) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Or a custom error
        }
        return ResponseEntity.ok(tender);
    }

    // Admin/Editor/Publisher access to get all tenders (including drafts, pending)
    @PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<TenderResponse>> getAllTenders() {
        List<TenderResponse> tenders = tenderService.getAllTenders();
        return ResponseEntity.ok(tenders);
    }

    // Admin/Editor/Publisher access to get tender by ID (for internal editing)
    @PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<TenderResponse> gettenderById(@PathVariable Long id) {
        TenderResponse tender = tenderService.getTenderById(id);
        return ResponseEntity.ok(tender);
    }

    // Admin/Editor/Publisher access to get tenders by status
    @PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<TenderResponse>> getTendersByStatus(@PathVariable ContentStatus status) {
        List<TenderResponse> tenders = tenderService.getTendersByStatus(status);
        return ResponseEntity.ok(tenders);
    }

    // Admin/Editor/Publisher access to create a new tender
    @PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @PostMapping(value = "/admin", consumes = {"multipart/form-data"})
    public ResponseEntity<TenderResponse> createtender(
            @RequestPart("tender") String tenderRequestJson,
            @RequestPart(value = "attachment", required = false) MultipartFile attachmentFile) throws IOException {
        TenderRequest request = objectMapper.readValue(tenderRequestJson, TenderRequest.class);
        TenderResponse newtender = tenderService.createTender(request, attachmentFile);
        return new ResponseEntity<>(newtender, HttpStatus.CREATED);
    }

    // Admin/Editor/Publisher access to update a tender
    @PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @PutMapping(value = "/admin/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<TenderResponse> updateTender(
            @PathVariable Long id,
            @RequestPart("tender") String tenderRequestJson,
            @RequestPart(value = "attachment", required = false) MultipartFile attachmentFile) throws IOException {
        TenderRequest request = objectMapper.readValue(tenderRequestJson, TenderRequest.class);
        TenderResponse updatedtender = tenderService.updateTender(id, request, attachmentFile);
        return ResponseEntity.ok(updatedtender);
    }

    // Publisher/Admin access to approve a tender
    @PreAuthorize("hasAnyAuthority('ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<TenderResponse> approvetender(@PathVariable Long id) {
        TenderResponse approvedtender = tenderService.approveTender(id);
        return ResponseEntity.ok(approvedtender);
    }

    // Publisher/Admin access to reject a tender
    @PreAuthorize("hasAnyAuthority('ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')")
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<TenderResponse> rejectTender(@PathVariable Long id) {
        TenderResponse rejectedtender = tenderService.rejectTender(id);
        return ResponseEntity.ok(rejectedtender);
    }

    // Admin access to delete a tender
    @PreAuthorize("hasAuthority('ROLE_PORTAL_ADMIN')") // Use hasAuthority for exact match
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deletetender(@PathVariable Long id) {
        tenderService.deleteTender(id);
        return ResponseEntity.noContent().build();
    }

    
    
    @GetMapping("public/latest")
    public ResponseEntity<TenderResponse> getLatesttender() {
    	TenderResponse tenderResponse = tenderService.getLatestTender();
       
        return ResponseEntity.ok(tenderResponse);
    }
}