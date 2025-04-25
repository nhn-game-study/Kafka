package com.alert.alert_front_server.adapter.in.rest.controller;

import com.alert.alert_front_server.adapter.in.rest.dto.NotificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void 알림등록_EMAIL_Success_테스트() throws Exception {
		// given
		NotificationRequest request = new NotificationRequest(
				"customer-456",
				"EMAIL",
				"test@example.com",
				"이메일 제목",
				"이메일 본문 내용입니다",
				"202503281200"
		);

		mockMvc.perform(post("/api/v1/notifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.notificationId").exists())
				.andExpect(jsonPath("$.status").value("QUEUED"))
				.andExpect(jsonPath("$.message").value("알림 발송등록 완료"));
	}

}
