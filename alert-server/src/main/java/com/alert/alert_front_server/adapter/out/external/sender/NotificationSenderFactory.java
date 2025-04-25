package com.alert.alert_front_server.adapter.out.external.sender;

import com.alert.alert_front_server.domain.ChannelType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 여러 NotificationSender 구현체를 모아서,
 * 특정 ChannelType에 맞는 Sender를 찾아주는 역할
 */
@Component
public class NotificationSenderFactory {

	private final Map<ChannelType, NotificationSender> senderMap;

	public NotificationSenderFactory(List<NotificationSender> senders) {
		this.senderMap = senders.stream()
				.collect(Collectors.toMap(NotificationSender::getChannelType, sender -> sender));
	}

	public NotificationSender getSender(ChannelType channelType) {
		return senderMap.get(channelType);
	}

}
