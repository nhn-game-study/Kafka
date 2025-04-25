package com.alert.alert_front_server.adapter.out.external.sender;

import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.domain.NotificationDomain;
import reactor.core.publisher.Mono;

public interface NotificationSender {
	ChannelType getChannelType();

	/**
	 * NotificationDomain을 받아 실제 발송을 시도하고,
	 * 결과를 Mono<Boolean>으로 반환한다. (true = 성공, false = 실패)
	 */
	Mono<Boolean> send(NotificationDomain notificationDomain);

}
