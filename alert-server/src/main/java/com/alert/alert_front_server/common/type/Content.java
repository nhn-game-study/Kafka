package com.alert.alert_front_server.common.type;

import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.exception.InvalidValueException;
import lombok.Getter;

@Getter
public class Content {
	private static final int CONTENT_MAX_SIZE = 1500;

	private String Content;

	public Content(String content) {

		if (content == null || content.isEmpty()) {
			throw new InvalidValueException(ErrorCode.INVALID_TITLE);
		}

		if(content.length() > CONTENT_MAX_SIZE) {
			throw new InvalidValueException(ErrorCode.TITLE_TOO_LONG);
		}

		this.Content = content;
	}

}
