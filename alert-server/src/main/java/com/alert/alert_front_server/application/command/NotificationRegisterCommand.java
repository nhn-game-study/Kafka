package com.alert.alert_front_server.application.command;

import com.alert.alert_front_server.adapter.in.rest.dto.NotificationRequest;
import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.common.type.Content;
import com.alert.alert_front_server.common.type.Title;
import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.exception.InvalidValueException;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Getter
public class NotificationRegisterCommand {
	private String customerId;
	private ChannelType channelType;
	private String destination;
	private Title title;
	private Content contents;
	private LocalDateTime scheduledTime;

	public NotificationRegisterCommand(NotificationRequest request) {
		validateMandatoryFields(request);

		this.customerId = request.customerId();
		this.channelType = ChannelType.fromString(request.channelType());

		channelType.validateDestination(request.destination());
		this.destination = request.destination();

		this.title = new Title(request.title());
		this.contents = new Content(request.contents());
		this.scheduledTime = getParsedScheduledTime(request.scheduledTime());
	}

	/**
	 * customerId, channelType, destination의 필수 값 체크
	 */
	private void validateMandatoryFields(NotificationRequest request) {
		if (!StringUtils.hasText(request.customerId()) ||
				!StringUtils.hasText(request.channelType()) ||
				!StringUtils.hasText(request.destination())) {
			throw new InvalidValueException(ErrorCode.INVALID_PARAMETER);
		}
	}

	/**
	 * yyyyMMddHHmm 포맷의 문자열을 LocalDateTime으로 변환
	 */
	private LocalDateTime getParsedScheduledTime(String scheduledTimeRaw) {
		if (scheduledTimeRaw == null || scheduledTimeRaw.isEmpty()) {
			return LocalDateTime.MIN;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		return LocalDateTime.parse(scheduledTimeRaw, formatter);
	}

}
