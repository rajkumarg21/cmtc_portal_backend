package com.cmtc.cms.dto.news;

import com.cmtc.cms.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleResponse {
    private Long id;
    private String titleHindi;
    private String titleEnglish;
    private String summaryHindi;
    private String summaryEnglish;
    private String contentHindi;
    private String contentEnglish;
    private String imageUrl;
    private String author;
    private LocalDate newsDate;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // This will map from NewsArticle.lastModifiedAt
    private ContentStatus status;
    private boolean isFeatured;
}