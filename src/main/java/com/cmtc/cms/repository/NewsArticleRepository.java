package com.cmtc.cms.repository;

import com.cmtc.cms.model.NewsArticle;
import com.cmtc.cms.model.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    List<NewsArticle> findByStatusOrderByNewsDateDesc(ContentStatus status);
    List<NewsArticle> findByStatusAndIsFeaturedTrueOrderByNewsDateDesc(ContentStatus status);
    List<NewsArticle> findByNewsDateBetweenAndStatusOrderByNewsDateDesc(LocalDate startDate, LocalDate endDate, ContentStatus status);
    Optional<NewsArticle> findByTitleEnglish(String titleEnglish); // For potential slug-like lookup if no explicit slug field
    Optional<NewsArticle> findByTitleHindi(String titleHindi);
    Optional<NewsArticle> findByIdAndStatus(Long id, ContentStatus status);
    List<NewsArticle> findByStatusAndIsFeaturedOrderByNewsDateDesc(ContentStatus status, boolean isFeatured);
    Optional<NewsArticle> findFirstByStatusOrderByPublishedAtDesc(ContentStatus status);
    List<NewsArticle> findByStatusOrderByPublishedAtDesc(ContentStatus status);

}
