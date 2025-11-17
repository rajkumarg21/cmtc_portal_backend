package com.cmtc.cms.dto.news;

import com.cmtc.cms.model.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleRequest {
    @NotBlank(message = "Title in Hindi cannot be blank")
    private String titleHindi;
    @NotBlank(message = "Title in English cannot be blank")
    private String titleEnglish;
    private String summaryHindi;
    private String summaryEnglish;
    @NotBlank(message = "Content in Hindi cannot be blank")
    private String contentHindi;
    @NotBlank(message = "Content in English cannot be blank")
    private String contentEnglish;
    private String imageUrl;
    private String author;
    @NotNull(message = "News date cannot be null")
    private LocalDate newsDate;
    @NotNull(message = "Status cannot be null")
    private ContentStatus status;
    private boolean isFeatured;
}