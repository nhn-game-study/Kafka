package com.alert.alert_front_server.common.response;

public enum ErrorCode {
	// 파라미터 조건 에러 코드 4000 ~
	INVALID_PARAMETER("4000", "Invalid parameter"),

	// 제목 관련 에러코드
	INVALID_TITLE("4000", "Title cannot be null or empty"),
	TITLE_TOO_LONG("4000", "title is too long"),

	// 내용 관련 에러코드
	INVALID_CONTENT("4000", "Content cannot be null or empty"),
	CONTENT_TOO_LONG("4000", "Content is too long"),

	// 채널 타입 관련 에러코드
	INVALID_CHANNEL_TYPE("4000", "ChannelType cannot be null or empty"),

	// 기타오류, 추후 필요하다면 ErrorCode 분리
	DATA_NOT_FOUND("9999", "entity not found"),
	DEFAULT_ERROR("9999", "default error");

	private final String code;
	private final String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
