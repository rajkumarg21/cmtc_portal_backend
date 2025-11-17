package com.cmtc.cms.dto.carousel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for the request payload when creating/updating a carousel slide.
 * This decouples the API contract from the internal entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarouselSlideRequest {
    private String title;
    private String link;
    private LocalDateTime toDate;
    private LocalDateTime fromDate;
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