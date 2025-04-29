package com.alert.alert_front_server.adapter.out.external;

import com.alert.alert_front_server.adapter.out.external.sender.NotificationSender;
import com.alert.alert_front_server.adapter.out.external.sender.NotificationSenderFactory;
import com.alert.alert_front_server.domain.NotificationDomain;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 기존 ExternalSenderService에서
 * '채널별 분기' 로직은 Sender 구현체들에게 위임
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalSenderService {

	private final NotificationSenderFactory senderFactory;

	@Retry(name = "sendRetry", fallbackMethod = "fallbackSend")
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

		// 채널별로 분기된 발송
		return sender.send(notificationDomain)
				.subscribeOn(Schedulers.boundedElastic()); // 비동기 처리 (스레드 풀에서 작업)
	}

	public Mono<Boolean> fallbackSend(NotificationDomain notificationDomain, Throwable throwable) {
		log.error("[ExternalSenderService] Fallback send invoked for notification ID: {}, due to error: {}",
				notificationDomain.getId(), throwable.getMessage());
		return Mono.just(false);
	}
}
