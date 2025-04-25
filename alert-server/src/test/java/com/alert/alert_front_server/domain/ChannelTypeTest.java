package com.alert.alert_front_server.domain;

import com.alert.alert_front_server.adapter.out.external.dto.EmailSendRequest;
import com.alert.alert_front_server.adapter.out.external.dto.NotificationSendRequest;
import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChannelTypeTest {
	@Test
	void EMAIL_문자열_fromString으로_변환하면_EMAIL_반환() {
		ChannelType channelType = ChannelType.fromString("EMAIL");
		assertThat(channelType).isEqualTo(ChannelType.EMAIL);
	}

	@Test
	void 채널타입이_null이면_InvalidValueException() {
		assertThatThrownBy(() -> ChannelType.fromString(null))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining(ErrorCode.INVALID_CHANNEL_TYPE.getMessage());
	}

	@Test
	void 유효하지_않은_채널타입이면_InvalidValueException() {
		assertThatThrownBy(() -> ChannelType.fromString("PUSH"))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining(ErrorCode.INVALID_CHANNEL_TYPE.getMessage());
	}

	@Test
	void EMAIL_createRequest_호출시_EmailSendRequest_객체_생성() {
		NotificationSendRequest request = ChannelType.EMAIL.createRequest("test@example.com", "제목", "내용");
		assertThat(request).isInstanceOf(EmailSendRequest.class);
	}

	@Test
	void UNKNOWN_createRequest_호출시_NotificationSendRequest_익명클래스_반환() {
		NotificationSendRequest request = ChannelType.UNKNOWN.createRequest("invalid", "제목", "내용");
		assertThat(request).isInstanceOf(NotificationSendRequest.class);
	}

	@Test
	void EMAIL_validateDestination_정상() {
		assertThatCode(() -> ChannelType.EMAIL.validateDestination("test@example.com"))
				.doesNotThrowAnyException();
	}

	@Test
	void EMAIL_validateDestination_비정상() {
		assertThatThrownBy(() -> ChannelType.EMAIL.validateDestination("test@@example"))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void UNKNOWN_validateDestination_호출시_예외발생() {
		assertThatThrownBy(() -> ChannelType.UNKNOWN.validateDestination("something"))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining(ErrorCode.INVALID_CHANNEL_TYPE.getMessage());
	}
}
