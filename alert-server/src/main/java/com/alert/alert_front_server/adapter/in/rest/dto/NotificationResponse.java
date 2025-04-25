package com.alert.alert_front_server.adapter.in.rest.dto;

import com.alert.alert_front_server.common.response.BaseResponse;
import lombok.Getter;

@Getter
public class NotificationResponse extends BaseResponse {
	private Long notificationId;
	private String status;  // REGISTERED, QUEUED, SENT, FAILED ...
	private String message;

	public NotificationResponse(Long notificationId, String status, String message) {
		super();
		this.notificationId = notificationId;
		this.status = status;
		this.message = message;
	}

	public NotificationResponse(String resultCode, String resultMessage, Long notificationId, String status, String message) {
		super(resultCode, resultMessage);
		this.notificationId = notificationId;
		this.status = status;
		this.message = message;
	}

}