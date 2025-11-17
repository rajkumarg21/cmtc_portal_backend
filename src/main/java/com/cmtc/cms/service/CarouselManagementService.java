package com.cmtc.cms.service;

import com.cmtc.cms.dto.carousel.CarouselSlideRequest;
import com.cmtc.cms.dto.carousel.CarouselSlideResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CarouselManagementService {
	 List<CarouselSlideResponse> getAllSlides();
	    List<CarouselSlideResponse> getAllPublicSlides();
	    Optional<CarouselSlideResponse> getSlideById(Long id);
	    CarouselSlideResponse createSlide(CarouselSlideRequest request, MultipartFile file) throws IOException;
	    Optional<CarouselSlideResponse> updateSlide(Long id, CarouselSlideRequest request, MultipartFile mediaFile) throws IOException;
	    void deleteSlide(Long id);
	    CarouselSlideResponse approveSlide(Long id);
	    void rejectSlide(Long id);
}
