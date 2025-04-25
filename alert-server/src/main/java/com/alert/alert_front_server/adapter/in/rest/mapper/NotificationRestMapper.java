package com.alert.alert_front_server.adapter.in.rest.mapper;

import com.alert.alert_front_server.adapter.in.rest.dto.NotificationHistoryResponse;
import com.alert.alert_front_server.adapter.in.rest.dto.NotificationRequest;
import com.alert.alert_front_server.adapter.in.rest.dto.NotificationResponse;
import com.alert.alert_front_server.application.command.NotificationRegisterCommand;
import com.alert.alert_front_server.domain.NotificationDomain;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class NotificationRestMapper {

	public NotificationRegisterCommand toCommand(NotificationRequest request) {
		return new NotificationRegisterCommand(request);
	}

	public NotificationResponse toResponse(NotificationDomain domain) {
		return new NotificationResponse(
				domain.getId(),
				domain.getStatus() != null ? domain.getStatus().name() : null,
				"알림 발송등록 완료"
		);
	}

	public NotificationHistoryResponse toHistoryResponse(Page<NotificationDomain> domainPage) {
		// 개별 아이템 변환
		var contentList = domainPage.getContent().stream()
				.map(this::toHistoryItem)
				.collect(Collectors.toList());

		NotificationHistoryResponse response = new NotificationHistoryResponse(
				domainPage.getNumber(),
				domainPage.getSize(),
				domainPage.getTotalPages(),
				domainPage.getTotalElements(),
				contentList
		);

		return response;
	}

	private NotificationHistoryResponse.NotificationItem toHistoryItem(NotificationDomain domain) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		var item = new NotificationHistoryResponse.NotificationItem(
				domain.getCustomerId(),
				domain.getChannelType() != null ? domain.getChannelType().name() : null,
				domain.getDestination(),
				domain.getTitle(),
				domain.getContents(),
				domain.getStatus() != null ? domain.getStatus().name() : null,
				domain.getCreatedAt() != null ? domain.getCreatedAt().format(formatter) : null,
				domain.getScheduledTime() != null ? domain.getScheduledTime().format(formatter) : null
		);

		return item;
	}
}
