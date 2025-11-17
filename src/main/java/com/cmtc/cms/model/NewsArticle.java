package com.cmtc.cms.model;

import com.cmtc.cms.model.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder; // Re-adding builder for convenience, you can remove if not using
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* 
 * Authore : Rajkumar Saad
 * 
 * */
@Entity
@Table(name = "news_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Re-added Builder for easier object creation in tests/factories
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titleHindi;

    @Column(nullable = false)
    private String titleEnglish;

    @Column(columnDefinition = "TEXT")
    private String summaryHindi;
    @Column(columnDefinition = "TEXT")
    private String summaryEnglish;

    @Column(columnDefinition = "TEXT")
    private String contentHindi;
    @Column(columnDefinition = "TEXT")
    private String contentEnglish;

    private String imageUrl;
    private String author;
    private LocalDate newsDate;

    @Enumerated(EnumType.STRING)
    private ContentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(nullable = false, updatable = false) // Set once, never updated via JPA directly
    private LocalDateTime createdAt;

    @Column(nullable = false) // Updated on any modification
    private LocalDateTime lastModifiedAt;

    private LocalDateTime publishedAt; // Set when status becomes PUBLISHED

    private boolean isFeatured;

    // Lifecycle callbacks for automatic timestamp management
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ContentStatus.PENDING_APPROVAL; // Default status for new articles
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedAt = LocalDateTime.now();
    }
}