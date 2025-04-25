package com.alert.alert_front_server.adapter.out.external;

import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.domain.NotificationDomain;
import com.alert.alert_front_server.domain.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@MockServerSettings(ports = {1080})
@TestPropertySource(properties = {
		"sender.base-url=http://localhost:1080"
})
class ExternalSenderServiceTest {

	@Autowired
	private ExternalSenderService externalSenderService;

	private MockServerClient mockServerClient;

	ExternalSenderServiceTest(MockServerClient client) {
		this.mockServerClient = client;
	}

	@BeforeEach
	void setupMockResponses() {
		mockServerClient.reset();
	}

	@Test
	void 이메일_발송_테스트() {
		mockServerClient.when(
				HttpRequest.request()
						.withMethod("POST")
						.withPath("/send/email")
		).respond(
				HttpResponse.response()
						.withStatusCode(200)
						.withHeader("Content-Type", "application/json")
						.withBody("{\"resultCode\":\"SUCCESS\"}")
		);

		NotificationDomain domain = new NotificationDomain(
				"3",
				ChannelType.EMAIL,
				"test@mockserver.com",
				"이메일 제목",
				"이메일 내용",
				null,
				NotificationStatus.REGISTERED,
				0
		);

		Mono<Boolean> resultMono = externalSenderService.send(domain);
		Boolean result = resultMono.block();

		assertThat(result).isTrue();

		mockServerClient.verify(
				HttpRequest.request()
						.withMethod("POST")
						.withPath("/send/email")
						.withBody(JsonBody.json(
								"{\"emailAddress\":\"test@mockserver.com\",\"title\":\"이메일 제목\",\"contents\":\"이메일 내용\"}"
						))
		);
	}

}
