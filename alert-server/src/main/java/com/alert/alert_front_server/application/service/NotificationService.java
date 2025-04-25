package com.alert.alert_front_server.application.service;

import com.alert.alert_front_server.adapter.out.persistence.mapper.NotificationDomainMapper;
import com.alert.alert_front_server.application.command.NotificationRegisterCommand;
import com.alert.alert_front_server.application.port.in.RegisterNotificationUseCase;
import com.alert.alert_front_server.application.port.out.SaveNotificationPort;
import com.alert.alert_front_server.application.port.out.UpdateNotificationPort;
import com.alert.alert_front_server.domain.NotificationDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 발송등록 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements RegisterNotificationUseCase {

	private final NotificationDomainMapper domainMapper;
	private final SaveNotificationPort saveNotificationPort;
	private final UpdateNotificationPort updateNotificationPort;

	@Override
	@Transactional
	public NotificationDomain registerNotification(NotificationRegisterCommand command) {
		LocalDateTime now = LocalDateTime.now();

		NotificationDomain domain = domainMapper.commandToDomain(command);

		// DB 저장 (REGISTERED)
		NotificationDomain saveNotificationDomain = saveNotificationPort.saveNotification(domain);

		// 즉시 발송
		if (saveNotificationDomain.isReadyToSchedule(now)) {
			saveNotificationDomain.markAsQueued();
			updateNotificationPort.updateNotification(saveNotificationDomain);
		}

		return saveNotificationDomain;
	}

}
