package com.alert.alert_front_server.adapter.in.rest.controller;

import com.alert.alert_front_server.adapter.in.rest.dto.NotificationRequest;
import com.alert.alert_front_server.adapter.in.rest.dto.NotificationResponse;
import com.alert.alert_front_server.adapter.in.rest.mapper.NotificationRestMapper;
import com.alert.alert_front_server.adapter.out.external.ExternalSenderService;
import com.alert.alert_front_server.application.port.in.RegisterNotificationUseCase;
import com.alert.alert_front_server.common.response.BaseResponse;
import com.alert.alert_front_server.domain.NotificationDomain;
import com.alert.alert_front_server.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

	private final RegisterNotificationUseCase registerNotification;
	private final NotificationRestMapper notificationRestMapper;
	private final NotificationEventPublisher notificationEventPublisher;

	/**
	 * '알림발송등록 API'
	 * @param request 클라이언트로부터 전달받은 알림 요청 데이터
	 * @return 알림 등록 결과를 포함한 응답 객체
	 */
	@PostMapping("/notifications")
	public ResponseEntity<BaseResponse> registerNotification(@RequestBody NotificationRequest request) {
		log.info("[NotificationController] Registering registerNotification");
		var command = notificationRestMapper.toCommand(request);
		NotificationDomain savedDomain = registerNotification.registerNotification(command);
		NotificationResponse response = notificationRestMapper.toResponse(savedDomain);

		notificationEventPublisher.publishNotificationEvent(savedDomain); // 이벤트 발행

		return ResponseEntity.ok(response);
	}

}
