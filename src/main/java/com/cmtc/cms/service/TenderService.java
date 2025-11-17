package com.cmtc.cms.service;

import com.cmtc.cms.dto.tender.TenderRequest;
import com.cmtc.cms.dto.tender.TenderResponse;
import com.cmtc.cms.model.enums.ContentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TenderService {
    TenderResponse createTender(TenderRequest request, MultipartFile attachmentFile);
    TenderResponse updateTender(Long id, TenderRequest request, MultipartFile attachmentFile);
    TenderResponse getTenderById(Long id);
    List<TenderResponse> getTendersByStatus(ContentStatus status);
    List<TenderResponse> getAllTenders();
    List<TenderResponse> getPublishedTenders();
    void deleteTender(Long id);
    TenderResponse approveTender(Long tenderId);
    TenderResponse rejectTender(Long tenderId);
	TenderResponse getLatestTender();
}
