package com.cmtc.cms.service.impl;

import com.cmtc.cms.dto.news.NewsArticleRequest;
import com.cmtc.cms.dto.news.NewsArticleResponse;
import com.cmtc.cms.dto.user.UserResponse;
import com.cmtc.cms.exception.ResourceNotFoundException;
import com.cmtc.cms.model.NewsArticle;
import com.cmtc.cms.model.User;
import com.cmtc.cms.model.enums.ContentStatus;
import com.cmtc.cms.repository.NewsArticleRepository;
import com.cmtc.cms.repository.UserRepository;
import com.cmtc.cms.service.NewsArticleService;
import com.cmtc.cms.service.FileStorageService; // Make sure this import is correct
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsArticleServiceImpl implements NewsArticleService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private FileStorageService fileStorageService; // This service now uses saveFile

    @Autowired
    private UserRepository userRepository;

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found.");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database: " + username));
    }

    private NewsArticleResponse convertToResponseDto(NewsArticle newsArticle) {
        NewsArticleResponse response = new NewsArticleResponse();
        BeanUtils.copyProperties(newsArticle, response);
        response.setUpdatedAt(newsArticle.getLastModifiedAt());
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getPublishedNewsArticles() {
        List<NewsArticle> newsArticles = newsArticleRepository
                .findByStatusOrderByPublishedAtDesc(ContentStatus.PUBLISHED);

        if (newsArticles.isEmpty()) {
            throw new ResourceNotFoundException("No published news articles found");
        }

        return newsArticles.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getFeaturedNewsArticles() {
        List<NewsArticle> featuredArticles = newsArticleRepository.findByStatusAndIsFeaturedOrderByNewsDateDesc(ContentStatus.PUBLISHED, true);
        return featuredArticles.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleResponse getPublishedNewsArticleById(Long id) {
    	NewsArticle newsArticle = newsArticleRepository
                .findFirstByStatusOrderByPublishedAtDesc(ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("No published news articles found"));
        return convertToResponseDto(newsArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleResponse> getAllNewsArticles() {
        List<NewsArticle> newsArticles = newsArticleRepository.findAll();
        return newsArticles.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NewsArticleResponse getNewsArticleById(Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News Article not found with ID: " + id));
        return convertToResponseDto(newsArticle);
    }

    @Override
    @Transactional
    public NewsArticleResponse createNewsArticle(NewsArticleRequest newsArticleRequest, MultipartFile imageFile) {
        NewsArticle newsArticle = new NewsArticle();
        BeanUtils.copyProperties(newsArticleRequest, newsArticle);
        newsArticle.setCreatedBy(getCurrentAuthenticatedUser());

        if (imageFile != null && !imageFile.isEmpty()) {
            // Changed from storeFile to saveFile
            String imageUrl = fileStorageService.saveFile(imageFile);
            newsArticle.setImageUrl(imageUrl);
        }

        NewsArticle savedArticle = newsArticleRepository.save(newsArticle);
        return convertToResponseDto(savedArticle);
    }

    @Override
    @Transactional
    public NewsArticleResponse updateNewsArticle(Long id, NewsArticleRequest newsArticleRequest, MultipartFile imageFile) {
        NewsArticle existingArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News Article not found with ID: " + id));

        BeanUtils.copyProperties(newsArticleRequest, existingArticle,
                                 "id", "createdAt", "createdBy", "approvedBy", "publishedAt");

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingArticle.getImageUrl() != null && !existingArticle.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(existingArticle.getImageUrl());
            }
            // Changed from storeFile to saveFile
            String newImageUrl = fileStorageService.saveFile(imageFile);
            existingArticle.setImageUrl(newImageUrl);
        } else if (newsArticleRequest.getImageUrl() == null && existingArticle.getImageUrl() != null) {
            fileStorageService.deleteFile(existingArticle.getImageUrl());
            existingArticle.setImageUrl(null);
        }

        existingArticle.setStatus(ContentStatus.PENDING_APPROVAL);
        existingArticle.setApprovedBy(null);
        existingArticle.setPublishedAt(null);

        NewsArticle updatedArticle = newsArticleRepository.save(existingArticle);
        return convertToResponseDto(updatedArticle);
    }

    @Override
    @Transactional
    public void deleteNewsArticle(Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News Article not found with ID: " + id));

        if (newsArticle.getImageUrl() != null && !newsArticle.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(newsArticle.getImageUrl());
        }

        newsArticleRepository.delete(newsArticle);
    }

    @Override
    @Transactional
    public NewsArticleResponse approveNewsArticle(Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News Article not found with ID: " + id));

        if (newsArticle.getStatus() == ContentStatus.PENDING_APPROVAL || newsArticle.getStatus() == ContentStatus.REJECTED) {
            newsArticle.setStatus(ContentStatus.PUBLISHED);
            newsArticle.setApprovedBy(getCurrentAuthenticatedUser());
            newsArticle.setPublishedAt(LocalDateTime.now());
            NewsArticle approvedArticle = newsArticleRepository.save(newsArticle);
            return convertToResponseDto(approvedArticle);
        } else {
            throw new IllegalStateException("News Article with ID " + id + " cannot be approved from its current status: " + newsArticle.getStatus());
        }
    }

    @Override
    @Transactional
    public NewsArticleResponse rejectNewsArticle(Long id) {
        NewsArticle newsArticle = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News Article not found with ID: " + id));

        if (newsArticle.getStatus() == ContentStatus.PENDING_APPROVAL || newsArticle.getStatus() == ContentStatus.PUBLISHED) {
            newsArticle.setStatus(ContentStatus.REJECTED);
            newsArticle.setApprovedBy(null);
            newsArticle.setPublishedAt(null);
            NewsArticle rejectedArticle = newsArticleRepository.save(newsArticle);
            return convertToResponseDto(rejectedArticle);
        } else {
            throw new IllegalStateException("News Article with ID " + id + " cannot be rejected from its current status: " + newsArticle.getStatus());
        }
    }
}
