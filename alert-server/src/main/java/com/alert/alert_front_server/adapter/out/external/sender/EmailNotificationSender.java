package com.alert.alert_front_server.adapter.out.external.sender;

import com.alert.alert_front_server.adapter.out.external.dto.EmailSendRequest;
import com.alert.alert_front_server.adapter.out.external.dto.SenderResponse;
import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.domain.NotificationDomain;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

	@Autowired
	private WebClient webClient;

	@Value("${sender.base-url}")
	private String senderBaseUrl;

	@Override
	public ChannelType getChannelType() {
		return ChannelType.EMAIL;
	}

	@Override
	public Mono<Boolean> send(NotificationDomain notificationDomain) {
		EmailSendRequest request = new EmailSendRequest(
				notificationDomain.getDestination(),
				notificationDomain.getTitle(),
				notificationDomain.getContents()
		);
		String url = senderBaseUrl + "/send/email";

		log.info("[EmailNotificationSender] Request URL: {}", url);

		return webClient.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(SenderResponse.class)
				.map(response -> {
					boolean success = "SUCCESS".equalsIgnoreCase(response.getResultCode());
					log.info("[EmailNotificationSender] resultCode={}, success={}", response.getResultCode(), success);
					return success;
				})
				.doOnError(e -> log.error("[EmailNotificationSender] Error sending SMS", e));
	}

}
