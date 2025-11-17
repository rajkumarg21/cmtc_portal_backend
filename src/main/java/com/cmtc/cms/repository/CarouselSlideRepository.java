package com.cmtc.cms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cmtc.cms.model.CarouselSlide;
import com.cmtc.cms.model.enums.ContentStatus;

@Repository
public interface CarouselSlideRepository extends JpaRepository<CarouselSlide, Long> {
    List<CarouselSlide> findByStatus(ContentStatus status);
}
