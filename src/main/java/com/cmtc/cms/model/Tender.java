package com.cmtc.cms.model;

import com.cmtc.cms.model.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String titleHindi;

    @Column(nullable = false)
    private String titleEnglish;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String attachmentUrl;
    private String attachmentFileName;

    private LocalDate tenderDate;
    private LocalDate archiveDate;


    @Enumerated(EnumType.STRING)
    private ContentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime publishedAt;
}