package com.cmtc.cms.service.impl;

import com.cmtc.cms.dto.tender.TenderRequest;
import com.cmtc.cms.dto.tender.TenderResponse;

import com.cmtc.cms.exception.ResourceNotFoundException;
import com.cmtc.cms.model.Tender;
import com.cmtc.cms.model.User;
import com.cmtc.cms.model.enums.ContentStatus;
import com.cmtc.cms.model.enums.UserRole;
import com.cmtc.cms.repository.TenderRepository;
import com.cmtc.cms.repository.UserRepository;
import com.cmtc.cms.service.TenderService;
import com.cmtc.cms.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.data.domain.Sort;   // for Sort.by(...)
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class TenderServiceImpl implements TenderService {

	   @Value("${file.upload-dir}")  
	    private String uploadDir;
	
    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public TenderResponse createTender(TenderRequest request, MultipartFile attachmentFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found."));

        Tender tender = new Tender();
        tender.setTitleHindi(request.getTitleHindi());
        tender.setTitleEnglish(request.getTitleEnglish());
        tender.setDescription(request.getDescription());
        tender.setTenderDate(request.getTenderDate());
        tender.setArchiveDate(request.getArchiveDate());
       

        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            String uploadedFileUrl = fileStorageService.saveFile(attachmentFile);
            tender.setAttachmentUrl(uploadedFileUrl);
            tender.setAttachmentFileName(attachmentFile.getOriginalFilename());
        } else {
            tender.setAttachmentFileName(request.getAttachmentFileName()); // If no new file, use name from request
        }

        tender.setStatus(ContentStatus.PENDING_APPROVAL);
        tender.setCreatedBy(currentUser);
        tender.setCreatedAt(LocalDateTime.now());
        tender.setLastModifiedAt(LocalDateTime.now());

        Tender savedTender = tenderRepository.save(tender);
        return convertToDto(savedTender);
    }

    @Override
    @Transactional
    public TenderResponse updateTender(Long id, TenderRequest request, MultipartFile attachmentFile) {
        Tender existingTender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + id));
        
        existingTender.setTitleHindi(request.getTitleHindi());
        existingTender.setTitleEnglish(request.getTitleEnglish());
        existingTender.setDescription(request.getDescription());
        existingTender.setTenderDate(request.getTenderDate());
        existingTender.setArchiveDate(request.getArchiveDate()); 

        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            if (existingTender.getAttachmentUrl() != null) {
                fileStorageService.deleteFile(existingTender.getAttachmentUrl());
            }
            String uploadedFileUrl = fileStorageService.saveFile(attachmentFile);
            existingTender.setAttachmentUrl(uploadedFileUrl);
            existingTender.setAttachmentFileName(attachmentFile.getOriginalFilename());
        } else if (request.getAttachmentFileName() == null && existingTender.getAttachmentUrl() != null) {
            // If frontend explicitly says to remove the file (e.g., filename is null/empty)
            fileStorageService.deleteFile(existingTender.getAttachmentUrl());
            existingTender.setAttachmentUrl(null);
            existingTender.setAttachmentFileName(null);
        } else {
            // No new file uploaded, and not explicitly removed. Keep existing file name.
            existingTender.setAttachmentFileName(request.getAttachmentFileName());
        }

        existingTender.setLastModifiedAt(LocalDateTime.now());
        if (existingTender.getStatus() == ContentStatus.PUBLISHED) {
             existingTender.setStatus(ContentStatus.PENDING_APPROVAL);
             existingTender.setPublishedAt(null);
             existingTender.setApprovedBy(null);
        }else {
        	 existingTender.setStatus(ContentStatus.PENDING_APPROVAL);
        }

        Tender updatedTender = tenderRepository.save(existingTender);
        return convertToDto(updatedTender);
    }

    @Override
    public TenderResponse getTenderById(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + id));
        return convertToDto(tender);
    }

    @Override
    public List<TenderResponse> getTendersByStatus(ContentStatus status) {
        return tenderRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    

    @Override
    public List<TenderResponse> getAllTenders() {
        return tenderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"))
                .stream()
                // Filter only tenders whose attachment exists in uploadDir
                .filter(tender -> attachmentExistsInUploadDir(tender.getAttachmentUrl()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private boolean attachmentExistsInUploadDir(String attachmentUrl) {
        try {
            if (attachmentUrl == null || attachmentUrl.isEmpty()) {
                return false; // eliminate records without attachment
            }

            String fileName = Paths.get(attachmentUrl).getFileName().toString();
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
System.out.println("filePath :  :   "+filePath);
            return Files.exists(filePath) && Files.isReadable(filePath);
        } catch (Exception e) {
            return false; // eliminate records if any error occurs
        }
    }

    @Override
    public List<TenderResponse> getPublishedTenders() {
        return tenderRepository.findByStatusOrderByPublishedAtDesc(ContentStatus.PUBLISHED)
                .stream()
                // Filter out tenders whose attachment file does not exist in uploadDir
               
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }  
    

    @Override
    @Transactional
    public void deleteTender(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + id));
        if (tender.getAttachmentUrl() != null) {
            fileStorageService.deleteFile(tender.getAttachmentUrl());
        }
        tenderRepository.delete(tender);
    }

    @Override
    @Transactional
    public TenderResponse approveTender(Long tenderId) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + tenderId));

        if (tender.getStatus() != ContentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Tender is not in PENDING_APPROVAL status.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approving user not found."));

        if (currentUser.getRole() != UserRole.PUBLISHER && currentUser.getRole() != UserRole.PORTAL_ADMIN) {
            throw new IllegalStateException("Only Publisher or Portal Admin can approve content.");
        }

        tender.setStatus(ContentStatus.PUBLISHED);
        tender.setApprovedBy(currentUser);
        tender.setPublishedAt(LocalDateTime.now());
        tender.setLastModifiedAt(LocalDateTime.now());

        Tender approvedTender = tenderRepository.save(tender);
        return convertToDto(approvedTender);
    }

    @Override
    @Transactional
    public TenderResponse rejectTender(Long tenderId) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + tenderId));

        if (tender.getStatus() != ContentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Tender is not in PENDING_APPROVAL status.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Rejecting user not found."));
        if (currentUser.getRole() != UserRole.PUBLISHER && currentUser.getRole() != UserRole.PORTAL_ADMIN) {
            throw new IllegalStateException("Only Publisher or Portal Admin can reject content.");
        }

        tender.setStatus(ContentStatus.REJECTED);
        tender.setLastModifiedAt(LocalDateTime.now());

        Tender rejectedTender = tenderRepository.save(tender);
        return convertToDto(rejectedTender);
    }

  

    private TenderResponse convertToDto(Tender tender) {
        TenderResponse dto = new TenderResponse();
        dto.setId(tender.getId());
        dto.setTitleHindi(tender.getTitleHindi());
        dto.setTitleEnglish(tender.getTitleEnglish());
        dto.setDescription(tender.getDescription());
        dto.setAttachmentUrl(tender.getAttachmentUrl());
        dto.setAttachmentFileName(tender.getAttachmentFileName());
        dto.setTenderDate(tender.getTenderDate());
        dto.setArchiveDate(tender.getArchiveDate());
        dto.setStatus(tender.getStatus());
        dto.setCreatedByUsername(tender.getCreatedBy() != null ? tender.getCreatedBy().getUsername() : null);
        dto.setApprovedByUsername(tender.getApprovedBy() != null ? tender.getApprovedBy().getUsername() : null);
        dto.setCreatedAt(tender.getCreatedAt());
        dto.setLastModifiedAt(tender.getLastModifiedAt());
        dto.setPublishedAt(tender.getPublishedAt());
        return dto;
    }

	@Override
	public TenderResponse getLatestTender() {
		Tender latest=tenderRepository.findTopByOrderByCreatedAtDesc();
		if (latest == null) {
	        return null; // or throw custom exception if no tender found
	    }
	    return convertToDto(latest);
	}
    

}
