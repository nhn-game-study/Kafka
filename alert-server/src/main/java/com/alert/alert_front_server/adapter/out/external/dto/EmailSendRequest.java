package com.alert.alert_front_server.adapter.out.external.dto;

import lombok.Getter;


/**
 * 이메일 전송용 DTO
 * emailAddress 필드 추가
 */
@Getter
public class EmailSendRequest extends NotificationSendRequest {
	private String emailAddress;

	public EmailSendRequest(String emailAddress, String title, String contents) {
		super(title, contents);
		this.emailAddress = emailAddress;
	}
}