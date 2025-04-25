package com.alert.alert_front_server.adapter.out.persistence;

import com.alert.alert_front_server.adapter.out.persistence.entity.NotificationEntity;
import com.alert.alert_front_server.adapter.out.persistence.mapper.NotificationDomainMapper;
import com.alert.alert_front_server.adapter.out.persistence.repository.NotificationRepository;
import com.alert.alert_front_server.application.port.out.SaveNotificationPort;
import com.alert.alert_front_server.application.port.out.UpdateNotificationPort;
import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.domain.NotificationDomain;
import com.alert.alert_front_server.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationAdapter implements SaveNotificationPort, UpdateNotificationPort {

	private final NotificationRepository notificationRepository;
	private final NotificationDomainMapper notificationDomainMapper;

	@Override
	public NotificationDomain saveNotification(NotificationDomain notificationDomain) {
		NotificationEntity notificationEntity = notificationDomainMapper.domainToEntity(notificationDomain);
		notificationEntity.setCreatedAt(LocalDateTime.now());
		NotificationEntity savedEntity = notificationRepository.save(notificationEntity);
		return notificationDomainMapper.entityToDomain(savedEntity);
	}

	@Override
	public void updateNotification(NotificationDomain notificationDomain) {
		NotificationEntity notificationEntity = notificationRepository.findById(notificationDomain.getId())
				.orElseThrow(() -> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));
		notificationEntity.updateStatusAndUpdatedAt(notificationDomain.getStatus().name());
	}

}
