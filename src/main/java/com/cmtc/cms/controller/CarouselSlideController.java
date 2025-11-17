package com.cmtc.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cmtc.cms.dto.carousel.CarouselSlideRequest;
import com.cmtc.cms.dto.carousel.CarouselSlideResponse;
import com.cmtc.cms.service.CarouselManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing CarouselSlide entities, following CMS conventions.
 * This class provides public and administrative endpoints for the carousel API.
 * It handles multipart file uploads and delegates business logic to the service layer.
 */
@RestController
@RequestMapping("/api/carousel")
public class CarouselSlideController {

    @Autowired
    private CarouselManagementService carouselManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    // Public endpoints to view carousel slides
    // No security required as these are for public consumption
    @GetMapping("/public")
    public ResponseEntity<List<CarouselSlideResponse>> getAllSlidesPublic() {
        List<CarouselSlideResponse> slides = carouselManagementService.getAllPublicSlides();
        return ResponseEntity.ok(slides);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<CarouselSlideResponse> getSlideByIdPublic(@PathVariable Long id) {
        CarouselSlideResponse slide = carouselManagementService.getSlideById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CarouselSlide not found with ID: " + id));
        return ResponseEntity.ok(slide);
    }

    // Admin access for management operations
    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<CarouselSlideResponse>> getAllSlidesAdmin() {
        List<CarouselSlideResponse> slides = carouselManagementService.getAllSlides();
        return ResponseEntity.ok(slides);
    }

    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<CarouselSlideResponse> getSlideByIdAdmin(@PathVariable Long id) {
        CarouselSlideResponse slide = carouselManagementService.getSlideById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CarouselSlide not found with ID: " + id));
        return ResponseEntity.ok(slide);
    }

    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @PostMapping(value = "/admin", consumes = {"multipart/form-data"})
    public ResponseEntity<CarouselSlideResponse> createSlide(
            @RequestPart("item") String carouselSlideRequestJson,
            @RequestPart("mediaFile") MultipartFile mediaFile) throws IOException {
        CarouselSlideRequest request = objectMapper.readValue(carouselSlideRequestJson, CarouselSlideRequest.class);
        CarouselSlideResponse newSlide = carouselManagementService.createSlide(request, mediaFile);
        return new ResponseEntity<>(newSlide, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @PutMapping(value = "/admin/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<CarouselSlideResponse> updateSlide(
            @PathVariable Long id,
            @RequestPart("item") String carouselSlideRequestJson,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) throws IOException {
        CarouselSlideRequest request = objectMapper.readValue(carouselSlideRequestJson, CarouselSlideRequest.class);
        Optional<CarouselSlideResponse> updatedSlide = carouselManagementService.updateSlide(id, request, mediaFile);
        return ResponseEntity.ok(updatedSlide.get());
    }

    @PreAuthorize("hasAnyRole('PUBLISHER', 'PORTAL_ADMIN')")
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<CarouselSlideResponse> approveSlide(@PathVariable Long id) {
        CarouselSlideResponse carouselSlideResponse = carouselManagementService.approveSlide(id);
        return ResponseEntity.ok(carouselSlideResponse);
    }
    @PreAuthorize("hasAnyRole('PUBLISHER', 'PORTAL_ADMIN')")
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<Void> rejectSlide(@PathVariable Long id) {
        carouselManagementService.rejectSlide(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteSlide(@PathVariable Long id) {
        carouselManagementService.deleteSlide(id);
        return ResponseEntity.ok().build();
    }
}
