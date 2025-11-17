package com.cmtc.cms.dto.tender;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenderRequest {
    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
    @NotBlank(message = "Title in Hindi cannot be blank")
    private String titleHindi;
    @NotBlank(message = "Title in English cannot be blank")
    private String titleEnglish;
    private String description;
    private String attachmentFileName;
    @NotNull(message = "Order Date cannot be null")
    private LocalDate tenderDate;
    private LocalDate archiveDate;
}
