package com.alert.alert_front_server.adapter.out.external;

import com.alert.alert_front_server.adapter.out.external.sender.NotificationSender;
import com.alert.alert_front_server.adapter.out.external.sender.NotificationSenderFactory;
import com.alert.alert_front_server.domain.NotificationDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 기존 ExternalSenderService에서
 * '채널별 분기' 로직은 Sender 구현체들에게 위임
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalSenderService {

	private final NotificationSenderFactory senderFactory;

	public Mono<Boolean> send(NotificationDomain notificationDomain) {
		if (notificationDomain.getChannelType() == null) {
			log.warn("[ExternalSenderService] ChannelType is null! id={}", notificationDomain.getId());
			return Mono.just(false);
		}

		NotificationSender sender = senderFactory.getSender(notificationDomain.getChannelType());

		if (sender == null) {
			log.warn("[ExternalSenderService] Unsupported channel: {}", notificationDomain.getChannelType());
			return Mono.just(false);
		}

		return sender.send(notificationDomain);
	}

}
