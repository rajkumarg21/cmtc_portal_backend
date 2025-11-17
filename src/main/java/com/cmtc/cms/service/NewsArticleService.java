package com.cmtc.cms.service;
import com.cmtc.cms.dto.news.NewsArticleRequest;
import com.cmtc.cms.dto.news.NewsArticleResponse;
import com.cmtc.cms.model.enums.ContentStatus;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface NewsArticleService {
	NewsArticleResponse createNewsArticle(NewsArticleRequest request, MultipartFile imageFile);
    List<NewsArticleResponse> getAllNewsArticles();
    List<NewsArticleResponse> getPublishedNewsArticles();
    List<NewsArticleResponse> getFeaturedNewsArticles();
    NewsArticleResponse getNewsArticleById(Long id);
    NewsArticleResponse getPublishedNewsArticleById(Long id);
    NewsArticleResponse updateNewsArticle(Long id, NewsArticleRequest request, MultipartFile imageFile);
    void deleteNewsArticle(Long id);
	NewsArticleResponse approveNewsArticle(Long id);
	NewsArticleResponse rejectNewsArticle(Long id);
}
