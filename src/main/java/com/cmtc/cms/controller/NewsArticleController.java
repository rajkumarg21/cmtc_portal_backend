package com.cmtc.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cmtc.cms.dto.news.NewsArticleRequest;
import com.cmtc.cms.dto.news.NewsArticleResponse;
import com.cmtc.cms.model.enums.ContentStatus; // Keep if used elsewhere, not directly in this snippet
import com.cmtc.cms.service.NewsArticleService;
import jakarta.validation.Valid; // Keep if used elsewhere, not directly in this snippet
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsArticleController {

    @Autowired
    private NewsArticleService newsArticleService;

    @Autowired
    private ObjectMapper objectMapper;

    // Public endpoint to get all published news articles
    @GetMapping("/public")
    public ResponseEntity<List<NewsArticleResponse>> getPublishedNewsArticles() {
        List<NewsArticleResponse> newsArticles = newsArticleService.getPublishedNewsArticles();
        return ResponseEntity.ok(newsArticles);
    }

    // Public endpoint to get featured news articles
    @GetMapping("/public/featured")
    public ResponseEntity<List<NewsArticleResponse>> getFeaturedNewsArticles() {
        List<NewsArticleResponse> newsArticles = newsArticleService.getFeaturedNewsArticles();
        return ResponseEntity.ok(newsArticles);
    }

    // Public endpoint to get a specific published news article by ID
    @GetMapping("/public/{id}")
    public ResponseEntity<NewsArticleResponse> getPublishedNewsArticleById(@PathVariable Long id) {
        NewsArticleResponse newsArticle = newsArticleService.getPublishedNewsArticleById(id);
        return ResponseEntity.ok(newsArticle);
    }

    // Admin/Publisher/Editor access to view all news articles (including drafts/archived)
    // Changed from hasAnyAuthority to hasAnyRole
    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<NewsArticleResponse>> getAllNewsArticles() {
        List<NewsArticleResponse> newsArticles = newsArticleService.getAllNewsArticles();
        return ResponseEntity.ok(newsArticles);
    }

    // Admin/Publisher/Editor access to get a specific news article by ID (for internal editing)
    // Changed from hasAnyAuthority to hasAnyRole
    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<NewsArticleResponse> getNewsArticleById(@PathVariable Long id) {
        NewsArticleResponse newsArticle = newsArticleService.getNewsArticleById(id);
        return ResponseEntity.ok(newsArticle);
    }

    // Admin/Publisher/Editor access to create a new news article
    // Changed from hasAnyAuthority to hasAnyRole
    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @PostMapping(value = "/admin", consumes = {"multipart/form-data"})
    public ResponseEntity<NewsArticleResponse> createNewsArticle(
            @RequestPart("newsArticle") String newsArticleRequestJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        NewsArticleRequest request = objectMapper.readValue(newsArticleRequestJson, NewsArticleRequest.class);
        NewsArticleResponse newNewsArticle = newsArticleService.createNewsArticle(request, imageFile);
        return new ResponseEntity<>(newNewsArticle, HttpStatus.CREATED);
    }

    // Admin/Publisher/Editor access to update an existing news article
    // Changed from hasAnyAuthority to hasAnyRole
    @PreAuthorize("hasAnyRole('EDITOR', 'PUBLISHER', 'PORTAL_ADMIN')")
    @PutMapping(value = "/admin/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<NewsArticleResponse> updateNewsArticle(
            @PathVariable Long id,
            @RequestPart("newsArticle") String newsArticleRequestJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        NewsArticleRequest request = objectMapper.readValue(newsArticleRequestJson, NewsArticleRequest.class);
        NewsArticleResponse updatedNewsArticle = newsArticleService.updateNewsArticle(id, request, imageFile);
        return ResponseEntity.ok(updatedNewsArticle);
    }

    // Admin access to delete a news article
    // Changed from hasAuthority to hasRole
    @PreAuthorize("hasRole('PORTAL_ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable Long id) {
        newsArticleService.deleteNewsArticle(id);
        return ResponseEntity.noContent().build();
    }
 // NEW: Endpoint to approve a news article
    // This typically requires PUBLISHER or PORTAL_ADMIN roles
    @PreAuthorize("hasAnyRole('ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')") // Added @PreAuthorize
    @PutMapping("/admin/approve/{id}") // Endpoint to match frontend call
    public ResponseEntity<NewsArticleResponse> approveNewsArticle(@PathVariable Long id) {
        NewsArticleResponse approvedArticle = newsArticleService.approveNewsArticle(id); // Call service method
        return ResponseEntity.ok(approvedArticle);
    }

    // Optional: Endpoint to reject a news article
    @PreAuthorize("hasAnyRole('ROLE_PUBLISHER', 'ROLE_PORTAL_ADMIN')") // Added @PreAuthorize
    @PutMapping("/admin/reject/{id}")
    public ResponseEntity<NewsArticleResponse> rejectNewsArticle(@PathVariable Long id) {
        NewsArticleResponse rejectedArticle = newsArticleService.rejectNewsArticle(id);
        return ResponseEntity.ok(rejectedArticle);
    }
}
