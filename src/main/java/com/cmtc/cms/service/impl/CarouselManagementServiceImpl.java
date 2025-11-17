package com.cmtc.cms.service.impl;

import com.cmtc.cms.dto.carousel.CarouselSlideRequest;
import com.cmtc.cms.dto.carousel.CarouselSlideResponse;
import com.cmtc.cms.exception.GenericException;
import com.cmtc.cms.helper.UserHelper;
import com.cmtc.cms.model.CarouselSlide;
import com.cmtc.cms.model.enums.ContentStatus;
import com.cmtc.cms.repository.CarouselSlideRepository;
import com.cmtc.cms.service.CarouselManagementService;
import com.cmtc.cms.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarouselManagementServiceImpl implements CarouselManagementService {

    private final CarouselSlideRepository carouselSlideRepository;
    private final FileStorageService fileStorageService;
    private final UserHelper userHelper;
    private static final String UPLOAD_DIR = "uploads";

    private CarouselSlideResponse toResponseDto(CarouselSlide slide) {
        return CarouselSlideResponse.builder()
                .id(slide.getId())
                .title(slide.getTitle())
                .link(slide.getRefLink())
                .imageUrl(slide.getImageUrl())
                .status(slide.getStatus())
                .createdAt(slide.getCreatedAt())
                .toDate(slide.getToDate())
                .fromDate(slide.getFromDate())
                .build();
    }

    @Override
    public List<CarouselSlideResponse> getAllSlides() {
        return carouselSlideRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarouselSlideResponse> getAllPublicSlides() {
        return carouselSlideRepository.findByStatus(ContentStatus.PUBLISHED).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CarouselSlideResponse> getSlideById(Long id) {
        return carouselSlideRepository.findById(id).map(this::toResponseDto);
    }

    @Override
    public CarouselSlideResponse createSlide(CarouselSlideRequest request, MultipartFile file) throws IOException {
        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = fileStorageService.saveFile(file);
        }
        if (request.getFromDate() != null && request.getToDate() != null) {
            if (request.getToDate().isBefore(request.getFromDate())) {
                throw new IllegalArgumentException("To Date cannot be before From Date");
            }
        }
        CarouselSlide newSlide = CarouselSlide.builder()
                .title(request.getTitle())
                .refLink(request.getLink())
                .imageUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .status(ContentStatus.PENDING_APPROVAL)
                .toDate(request.getToDate())
                .fromDate(request.getFromDate())
                .approvedBy(null)
                .approvedAt(null)
                .build();

        return toResponseDto(carouselSlideRepository.save(newSlide));
    }

    @Override
    public Optional<CarouselSlideResponse> updateSlide(Long id, CarouselSlideRequest request, MultipartFile mediaFile) throws IOException {
        return carouselSlideRepository.findById(id).map(slide -> {
            slide.setTitle(request.getTitle());
            slide.setRefLink(request.getLink());
            slide.setToDate(request.getToDate());
            slide.setFromDate(request.getFromDate());
            slide.setStatus(ContentStatus.PENDING_APPROVAL);
            // reset approval on update
            slide.setApprovedBy(null);
            slide.setApprovedAt(null);

            if (mediaFile != null && !mediaFile.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(mediaFile.getOriginalFilename());
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(mediaFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    slide.setImageUrl("/" + UPLOAD_DIR + "/" + fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store new image file", e);
                }
            }
            return toResponseDto(carouselSlideRepository.save(slide));
        });
    }

    @Override
    public void deleteSlide(Long id) {
        if (!carouselSlideRepository.existsById(id)) {
            throw new IllegalStateException("CarouselSlide with ID " + id + " does not exist.");
        }
        carouselSlideRepository.deleteById(id);
    }

    @Override
    public CarouselSlideResponse approveSlide(Long id) {
        Optional<CarouselSlide> slideOpt = carouselSlideRepository.findById(id);
        if (slideOpt.isPresent()) {
            CarouselSlide slide = slideOpt.get();
            slide.setStatus(ContentStatus.PUBLISHED);
            slide.setApprovedBy(userHelper.getCurrentAuthenticatedUser().getId());
            slide.setApprovedAt(LocalDateTime.now());  // Consider using a fixed clock or ZonedDateTime
            return toResponseDto(carouselSlideRepository.save(slide)); // Returning the saved slide
        } else {
            // Log the error or throw an exception if needed
            throw new GenericException("Slide with id " + id + " not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void rejectSlide(Long id) {
        carouselSlideRepository.findById(id).ifPresent(slide -> {
            slide.setStatus(ContentStatus.REJECTED);
            slide.setModifiedBy(userHelper.getCurrentAuthenticatedUser().getId());
            slide.setModifiedAt(LocalDateTime.now());
            carouselSlideRepository.save(slide);
        });
    }

}
