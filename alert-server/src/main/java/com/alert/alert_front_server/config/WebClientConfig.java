package com.alert.alert_front_server.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		// 1) TCP 클라이언트 설정
		TcpClient tcpClient = TcpClient.create()
				// 연결 타임아웃(밀리초)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5초
				// 커넥션 후 Read/Write 타임아웃
				.doOnConnected(connection ->
						connection
								.addHandlerLast(new ReadTimeoutHandler(10))  // 10초
								.addHandlerLast(new WriteTimeoutHandler(10)) // 10초
				);

		// 2) HttpClient에 tcpClient 반영 & 커넥션 풀 설정
		HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("custom-pool")
						.maxConnections(200)               // 최대 커넥션 수
						.pendingAcquireMaxCount(50)       // 커넥션 부족 시 대기 가능한 acquire 요청 수
						.pendingAcquireTimeout(Duration.ofSeconds(5))
						.build())
				.tcpConfiguration(client -> tcpClient);

		// 3) WebClient에 HttpClient 연결
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
}
