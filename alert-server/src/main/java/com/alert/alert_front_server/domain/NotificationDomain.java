package com.alert.alert_front_server.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationDomain {
	private Long id;
	private String customerId;
	private ChannelType channelType; // SMS, KAKAOTALK, EMAIL 등
	private String destination; // 전화번호/카톡ID/이메일 등
	private String title;
	private String contents;

	private LocalDateTime createdAt;
	private LocalDateTime scheduledTime;

	private NotificationStatus status;
	private int retryCount;

	public NotificationDomain(Long id, String customerId, ChannelType channelType, String destination, String title, String contents, LocalDateTime scheduledTime, NotificationStatus status, int retryCount, LocalDateTime createdAt) {
		this.id = id;
		this.customerId = customerId;
		this.channelType = channelType;
		this.destination = destination;
		this.title = title;
		this.contents = contents;
		this.scheduledTime = scheduledTime;
		this.status = status;
		this.retryCount = retryCount;
		this.createdAt = createdAt;
	}

	public NotificationDomain(String customerId, ChannelType channelType, String destination, String title, String contents, LocalDateTime scheduledTime, NotificationStatus status, int retryCount) {
		this.customerId = customerId;
		this.channelType = channelType;
		this.destination = destination;
		this.title = title;
		this.contents = contents;
		this.scheduledTime = scheduledTime;
		this.status = status;
		this.retryCount = retryCount;
		this.createdAt = LocalDateTime.now();
	}

	public void markAsQueued() {
		this.status = NotificationStatus.QUEUED;
	}

	public void markAsFailed() {
		this.status = NotificationStatus.FAILED;
		this.retryCount++;
	}

	public void markAsSent() {
		this.status = NotificationStatus.SENT;
	}

	public boolean isReadyToSchedule(LocalDateTime now) {
		return this.status == NotificationStatus.REGISTERED && this.scheduledTime != null && this.scheduledTime.isBefore(now);
	}

}
