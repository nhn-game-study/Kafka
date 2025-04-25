package com.alert.alert_front_server.adapter.in.rest.dto;

public record NotificationRequest(
		String customerId,
		String channelType,   // SMS/KAKAOTALK/EMAIL
		String destination,
		String title,
		String contents,
		// yyyyMMddHHmm 형태 (null or empty이면 즉시발송)
		String scheduledTime
) {
}