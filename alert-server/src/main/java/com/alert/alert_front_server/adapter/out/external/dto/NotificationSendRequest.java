package com.alert.alert_front_server.adapter.out.external.dto;

import lombok.*;

/**
 * 공통 요청 DTO (추상 클래스)
 * title, contents는 모든 채널에서 공통
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class NotificationSendRequest {
	private String title;
	private String contents;
}
