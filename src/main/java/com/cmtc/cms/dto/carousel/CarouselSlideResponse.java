package com.cmtc.cms.dto.carousel;

import com.cmtc.cms.model.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for the response payload when returning carousel slide data.
 * This can be used to control what data is exposed to the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarouselSlideResponse {
    private Long id;
    private String title;
    private String link;
    private ContentStatus status;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime toDate;
    private LocalDateTime fromDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public ContentStatus getStatus() {
		return status;
	}
	public void setStatus(ContentStatus status) {
		this.status = status;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}
	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}
	public LocalDateTime getToDate() {
		return toDate;
	}
	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}
	public LocalDateTime getFromDate() {
		return fromDate;
	}
	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}	 
    
}

