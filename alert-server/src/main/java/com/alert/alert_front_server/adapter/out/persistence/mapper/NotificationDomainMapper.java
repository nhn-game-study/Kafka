package com.alert.alert_front_server.adapter.out.persistence.mapper;

import com.alert.alert_front_server.adapter.out.persistence.entity.NotificationEntity;
import com.alert.alert_front_server.application.command.NotificationRegisterCommand;
import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.domain.NotificationDomain;
import com.alert.alert_front_server.domain.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationDomainMapper {

	public NotificationEntity domainToEntity(NotificationDomain domain) {
		return NotificationEntity.builder()
				.customerId(domain.getCustomerId())
				.channelType((domain.getChannelType().name()))
				.destination(domain.getDestination())
				.title(domain.getTitle())
				.contents(domain.getContents())
				.updatedAt(LocalDateTime.now())
				.scheduledTime(domain.getScheduledTime())
				.status(domain.getStatus() != null ? domain.getStatus().name() : null)
				.retryCount(domain.getRetryCount())
				.build();
	}

	public NotificationDomain entityToDomain(NotificationEntity entity) {
		return new NotificationDomain(
				entity.getId(),
				entity.getCustomerId(),
				ChannelType.fromString(entity.getChannelType()),
				entity.getDestination(),
				entity.getTitle(),
				entity.getContents(),
				entity.getScheduledTime(),
				entity.getStatus() != null ? NotificationStatus.valueOf(entity.getStatus()) : null,
				entity.getRetryCount(),
				entity.getCreatedAt()
		);
	}

	public NotificationDomain commandToDomain(NotificationRegisterCommand command) {
		return new NotificationDomain(
				command.getCustomerId(),
				command.getChannelType(),
				command.getDestination(),
				command.getTitle().getTitle(),
				command.getContents().getContent(),
				command.getScheduledTime(),
				NotificationStatus.REGISTERED,
				0
		);
	}

}