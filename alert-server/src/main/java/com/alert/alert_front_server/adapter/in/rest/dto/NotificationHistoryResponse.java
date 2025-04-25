package com.alert.alert_front_server.adapter.in.rest.dto;

import com.alert.alert_front_server.common.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationHistoryResponse extends BaseResponse {

	private int currentPage;
	private int pageSize;
	private int totalPages;
	private long totalElements;

	private List<NotificationItem> content;

	public NotificationHistoryResponse(int currentPage, int pageSize, int totalPages, long totalElements,
									   List<NotificationItem> content) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
		this.content = content;
	}

	/**
	 * 알림 목록 개별 아이템 DTO
	 */
	@Getter
	@AllArgsConstructor
	public static class NotificationItem {
		private String customerId;
		private String channelType;
		private String destination;
		private String title;
		private String contents;
		private String status;
		private String createdAt;    // 문자열 포맷
		private String schaduledTime;
	}

}
