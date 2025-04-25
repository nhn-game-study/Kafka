package com.alert.alert_front_server.domain;

import com.alert.alert_front_server.adapter.out.external.dto.EmailSendRequest;
import com.alert.alert_front_server.adapter.out.external.dto.NotificationSendRequest;
import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.exception.InvalidValueException;

import java.util.regex.Pattern;

/**
 * 알림 채널 유형 enum
 * 각 상수가 createRequest()를 오버라이드해, 적절한 자식 DTO를 생성
 */
public enum ChannelType {

	EMAIL {
		// 이메일 형식 예시
		private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9.-]+$");

		@Override
		public NotificationSendRequest createRequest(String destination, String title, String contents) {
			return new EmailSendRequest(destination, title, contents);
		}

		@Override
		public void validateDestination(String destination) {
			if (!EMAIL_PATTERN.matcher(destination).matches()) {
				throw new InvalidValueException(ErrorCode.INVALID_PARAMETER);
			}
		}
	},
	UNKNOWN {
		@Override
		public NotificationSendRequest createRequest(String destination, String title, String contents) {
			// 지원하지 않는 채널일 경우, 기본 추상 DTO를 리턴하거나 별도의 로직 처리
			return new NotificationSendRequest(title, contents) {};
		}

		@Override
		public void validateDestination(String destination) {
			// 지원하지 않는 채널 -> 예외 처리 또는 로깅 등
			throw new InvalidValueException(ErrorCode.INVALID_CHANNEL_TYPE);
		}
	};

	/**
	 * 채널별 Request 생성 (기존 코드와 동일)
	 */
	public abstract NotificationSendRequest createRequest(String destination, String title, String contents);

	/**
	 * 채널별 Destination 유효성 검사
	 */
	public abstract void validateDestination(String destination);

	/**
	 * 문자열 -> enum 변환
	 * 일치하지 않으면 UNKNOWN 반환 (또는 예외 던지는 식으로 처리해도 됨)
	 */
	public static ChannelType fromString(String channelType) {
		if (channelType == null) {
			throw new InvalidValueException(ErrorCode.INVALID_CHANNEL_TYPE);
		}
		try {
			return ChannelType.valueOf(channelType.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidValueException(ErrorCode.INVALID_CHANNEL_TYPE);
		}
	}
}
