package com.cmtc.cms.repository;

import com.cmtc.cms.model.Tender;
import com.cmtc.cms.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {
    List<Tender> findByStatus(ContentStatus status);
    List<Tender> findByStatusOrderByTenderDateDesc(ContentStatus status);
    //List<Tender> findByIsAlertTrueAndStatusOrderByTenderDateDesc(ContentStatus status); // For alerts
    //List<Tender> findByCategoryIdAndStatusOrderByTenderDateDesc(Long categoryId, ContentStatus status);
	Tender findTopByOrderByCreatedAtDesc();
	List<Tender> findByStatusOrderByPublishedAtDesc(ContentStatus status);
}