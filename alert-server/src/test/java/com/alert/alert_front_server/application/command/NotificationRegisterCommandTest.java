package com.alert.alert_front_server.application.command;

import com.alert.alert_front_server.adapter.in.rest.dto.NotificationRequest;
import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.common.type.Content;
import com.alert.alert_front_server.common.type.Title;
import com.alert.alert_front_server.domain.ChannelType;
import com.alert.alert_front_server.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRegisterCommandTest {

	@Test
	void EMAIL_채널_이메일_형식_비정상이면_예외() {
		NotificationRequest request = new NotificationRequest(
				"customerId",
				"email",
				"not-an-email",
				"제목",
				"내용",
				"202501010900"
		);

		assertThatThrownBy(() -> new NotificationRegisterCommand(request))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining(ErrorCode.INVALID_PARAMETER.getMessage());
	}

}
