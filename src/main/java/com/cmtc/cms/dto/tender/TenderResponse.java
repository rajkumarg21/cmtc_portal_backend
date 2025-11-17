package com.cmtc.cms.dto.tender;

import com.cmtc.cms.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenderResponse {
    private Long id;
   
    private String titleHindi;
    private String titleEnglish;
    private String description;
    private String attachmentUrl;
    private String attachmentFileName;
    private LocalDate tenderDate;
    private LocalDate archiveDate;
    private ContentStatus status;
    private String createdByUsername;
    private String approvedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime publishedAt;
    
}